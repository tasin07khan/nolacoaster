package util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;

/**
 * The purpose of the throwable findder is to find and report on the
 * types in a code base that are descendants of java.lang.Throwable.
 * As best as possible, we attempt to record whether or not each
 * class is a.) a regular class, b.) an anonymous class.
 * 
 * @author Nels E. Beckman
 * 
 * @see {@link java.lang.Throwable}
 */
public final class ThrowableFinder extends AbstractCompilationUnitAnalysis {
	
	@Override
	public void analyzeCompilationUnit(CompilationUnit d) {
		AST ast = d.getAST();
		final ITypeBinding object = ast.resolveWellKnownType(Object.class.getCanonicalName());
		d.accept(new ASTVisitor(){
			@Override
			public void endVisit(AnonymousClassDeclaration node) {
				if( isThrowable(node.resolveBinding().getSuperclass(), object) ) {
					System.out.println("IsThrowable: Anonymous throwable subclass.");
				}
			}

			@Override
			public void endVisit(TypeDeclaration node) {
				// We are examining the JSL, so the type itself could be throwable!
				ITypeBinding resolveBinding = node.resolveBinding();
				if( node.isInterface() ) {
					return;
				}
				else if( isThrowable(resolveBinding, object) ) {
					System.out.println("IsThrowable: Throwable subclass: " + resolveBinding.getQualifiedName());
				}
			}
		});
	}

	/** Is the given type or any of its superclasses java.lang.Throwable? */
	private static boolean isThrowable(ITypeBinding type, ITypeBinding object) {
		if( type.getQualifiedName().equals(Throwable.class.getCanonicalName()) ) {
			return true;
		}
		else if( !type.equals(object) ) {
			return isThrowable(type.getSuperclass(), object);
		}
		else {
			return false;
		}
	}
	
}
