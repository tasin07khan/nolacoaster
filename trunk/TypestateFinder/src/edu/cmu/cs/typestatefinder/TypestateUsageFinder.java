package edu.cmu.cs.typestatefinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;

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
	private final Set<String> protocolMethods;
	
	private static final String INPUT_FILE_PATH = "C:\\Users\\nbeckman\\workspace\\TypestateFinder\\protocol_methods.txt";
	
	public TypestateUsageFinder() {
		super();
		TreeSet<String> result = new TreeSet<String>();
		
		// Load file, each line is an entry
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(INPUT_FILE_PATH)));
			String cur_line;
			while( (cur_line = br.readLine()) != null ) {
				cur_line = cur_line.trim();
				if( !"".equals(cur_line) )
					result.add(cur_line);
			}
			this.protocolMethods = Collections.unmodifiableSet(result);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void analyzeCompilationUnit(CompilationUnit cu) {	
		// Also, when are fields of classes fields that define protocols?
		cu.accept(new ProtocolFieldVisitor());
	}
	
	private class ProtocolFieldVisitor extends ASTVisitor {
		private final Deque<ITypeBinding> currentType = new LinkedList<ITypeBinding>();
		
		private boolean isAnonymous() {
			return currentType.peek().isAnonymous();
		}
		
		private String mostRecentType() {
			for( ITypeBinding type : currentType ) {
				if( type.isAnonymous() ) continue;
				else return type.getQualifiedName();
			}
			throw new IllegalStateException("Invariant violated");
		}
		
		private ITypeBinding curType() {
			return currentType.peek();
		}
		
		@Override
		public boolean visit(AnonymousClassDeclaration node) {
			currentType.push(node.resolveBinding());
			return true;
		}
		
		@Override
		public boolean visit(TypeDeclaration node) {
			currentType.push(node.resolveBinding());
			return true;
		}
		
		@Override
		public boolean visit(EnumDeclaration node) {
			currentType.push(node.resolveBinding());
			return true;
		}
		
		@Override public void endVisit(EnumDeclaration node) { this.currentType.pop(); }
		@Override public void endVisit(AnonymousClassDeclaration node) {this.currentType.pop();}
		@Override public void endVisit(TypeDeclaration node) {this.currentType.pop();}

		
		@Override
		public void endVisit(MethodInvocation node) {
			// Is this method defined in a class that is
			// in the protocol set? Are any of its parents?
			ITypeBinding declaring_type = node.resolveMethodBinding().getDeclaringClass();
			ITypeBinding current_type = curType();

			// We really only care if the method is being called from outside of this class.
			if( !declaring_type.equals(current_type) ) {
				String method_key = TypestateFinder.methodSig(node.resolveMethodBinding(), 
						TypestateFinder.removeStaticArgs(declaring_type.getQualifiedName()));
				if( protocolMethods.contains(method_key) ) {
					// OUPUT
					reportUse(method_key, declaring_type.getQualifiedName(), node);
				}
			}
		}
		
		private void reportUse(String method_called, String class_defining_method, ASTNode node) {
			
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
			// This type, isAnonymous, resource, line number, class called, method called
			String this_type = mostRecentType();
			String is_anon = Boolean.toString(isAnonymous());
			String output_str = this_type + ", " + is_anon + ", " + resource_name + ", " + 
				line_no + ", " + method_called;

			System.out.println("ProtocolClassCalled: " + output_str);
		}
	}
	
	/**
	 * Does the given type define a protocol or do any of its super
	 * types? If yes, return the fully-qualified type name, and if
	 * no, return NONE. (Fully qualified name is returned since it
	 * may be of a supertype, and not of the given type parameter.)
	 */
//	private Option<String> findTypeIfInProtocols(ITypeBinding type) {
//		String type_name = type.getQualifiedName();
//		if( protocolMethods.contains(type_name) ) {
//			return Option.some(type_name);
//		}
//		else {
//			// Try super-types.
//			if( type.getSuperclass() != null ) {
//				Option<String> super_name = findTypeIfInProtocols(type.getSuperclass());
//				if( super_name.isSome() ) return super_name;
//			}
//			
//			// Try interfaces
//			for( ITypeBinding interface_type : type.getInterfaces() ) {
//				Option<String> inter_name = findTypeIfInProtocols(interface_type);
//				if( inter_name.isSome() ) return inter_name;
//			}
//			
//			return Option.none();
//		}
//	}	
}