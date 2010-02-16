package edu.cmu.cs.typestatefinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.IAnalysisReporter.SEVERITY;
import edu.rice.cs.plt.tuple.Option;

/**
 * From a file listing all the classes that define typestate, list all
 * of the classes that use typestate, and the number of types that methods
 * of those classes were called.
 * 
 * @author Nels E. Beckman
 *
 */
public final class TypestateUsageFinder extends AbstractCompilationUnitAnalysis {

	/**
	 * A set of class names that define protocols.
	 */
	private final Set<String> classesDefiningProts;
	
	private static final String INPUT_FILE_PATH = "C:\\Users\\nbeckman\\workspace\\TypestateFinder\\classes_with_protocols.txt";
	
	public TypestateUsageFinder() {
		super();
		TreeSet<String> result = new TreeSet<String>();
		
		// Load file, each line is an entry
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(INPUT_FILE_PATH)));
			String cur_line;
			while( (cur_line = br.readLine()) != null ) {
				result.add(cur_line.trim());
			}
			this.classesDefiningProts = Collections.unmodifiableSet(result);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void analyzeCompilationUnit(CompilationUnit cu) {	
		cu.accept(new ASTVisitor() {
			@Override
			public void endVisit(MethodDeclaration d) {
				// If we get this far, classesDefiningProts is initialized.
				// Analyze method body...
				d.accept(new CallSiteVisitor(d));
			}
		});
		
		// Also, when are fields of classes fields that define protocols?
		cu.accept(new ASTVisitor() {
			@Override
			public void endVisit(FieldDeclaration node) {
				if( Modifier.isStatic(node.getModifiers()) ) return;
				
				for( Object frag_ : node.fragments() ) {
					VariableDeclarationFragment frag = (VariableDeclarationFragment)frag_;
					ITypeBinding field_binding = frag.resolveBinding().getType();
					Option<String> fq_type = findTypeIfInProtocols(field_binding);
					if( fq_type.isSome() ) {
						String error_msg = "Field type (" + fq_type.unwrap() + ")defines a protocol.";
						reporter.reportUserProblem(error_msg, node, getName(), SEVERITY.WARNING);
					}
				}
			}
		});
	}
	
	/**
	 * Does the given type define a protocol or do any of its super
	 * types? If yes, return the fully-qualified type name, and if
	 * no, return NONE. (Fully qualified name is returned since it
	 * may be of a supertype, and not of the given type parameter.)
	 */
	private Option<String> findTypeIfInProtocols(ITypeBinding type) {
		String type_name = type.getQualifiedName();
		if( classesDefiningProts.contains(type_name) ) {
			return Option.some(type_name);
		}
		else {
			// Try super-types.
			if( type.getSuperclass() != null ) {
				Option<String> super_name = findTypeIfInProtocols(type.getSuperclass());
				if( super_name.isSome() ) return super_name;
			}
			
			// Try interfaces
			for( ITypeBinding interface_type : type.getInterfaces() ) {
				Option<String> inter_name = findTypeIfInProtocols(interface_type);
				if( inter_name.isSome() ) return inter_name;
			}
			
			return Option.none();
		}
	}
	
	// Note this class is not static deliberately.
	private class CallSiteVisitor extends ASTVisitor {
		final private MethodDeclaration methodDeclaration;
		public CallSiteVisitor(MethodDeclaration d) {
			this.methodDeclaration = d;
		}
		
		@Override
		public void endVisit(ClassInstanceCreation node) {
			// Is this method defined in a class that is
			// in the protocol set? Are any of its parents?
			ITypeBinding declaring_type = node.resolveConstructorBinding().getDeclaringClass();
			Option<String> type_name = findTypeIfInProtocols(declaring_type);
			
			if( type_name.isSome() ) {
				// OUTPUT
				reportUse(node.resolveConstructorBinding(), type_name.unwrap(), 
						  methodDeclaration.resolveBinding().getDeclaringClass(), 
						  this.methodDeclaration, node);
			}
		}

		@Override
		public void endVisit(MethodInvocation node) {
			// Is this method defined in a class that is
			// in the protocol set? Are any of its parents?
			ITypeBinding declaring_type = node.resolveMethodBinding().getDeclaringClass();
			Option<String> type_name = findTypeIfInProtocols(declaring_type);
			
			if( type_name.isSome() ) {
				// OUTPUT
				reportUse(node.resolveMethodBinding(), type_name.unwrap(), 
						  methodDeclaration.resolveBinding().getDeclaringClass(), 
						  this.methodDeclaration, node);
			}
		}
		
		private void reportUse(IMethodBinding method_called, String class_defining_method, 
				ITypeBinding this_class, MethodDeclaration this_method, ASTNode node) {
			// FQ Methodname, type_name, method_called_from, resource name, line no., 
			String method_called_ = method_called.getDeclaringClass().getQualifiedName() + "." + method_called.getName();
			// Calling class
			String this_class_ = this_class.getName() == null ? "XXX" : this_class.getQualifiedName();
			// Method called from
			// Can't believe I HAVE to call getFullyQualifiedName for the unqialified name...
			String this_method_ = this_class_ + "." + this_method.getName().getFullyQualifiedName();
			
			// Identify the closest resource to the ASTNode,
			ASTNode root = node.getRoot();
			IResource resource;
			
			if( root.getNodeType() != ASTNode.COMPILATION_UNIT ) System.err.println("Problem: No compilation unit.");
			
			CompilationUnit cu = (CompilationUnit) root;
			IJavaElement je = cu.getJavaElement();
			resource = je.getResource();
			cu.getPackage();

			String resource_name = resource.getName();
			int line_no = cu.getLineNumber(node.getStartPosition());

			// Now we can output.
			String output_str = method_called_ + ", " + class_defining_method + ", " + 
				this_method_ + ", " + resource_name + ", " + line_no;
			reporter.reportUserProblem(output_str, node, getName());
		}
	}
}