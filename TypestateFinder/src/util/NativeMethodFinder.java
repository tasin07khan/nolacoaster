package util;

import java.lang.reflect.Modifier;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;

/**
 * Prints the number of native methods we found, and the total number of methods found.
 * @author Nels E. Beckman
 *
 */
public final class NativeMethodFinder extends AbstractCrystalMethodAnalysis {

	// Ugh, statics...
	private static int numberNativeMethods = 0;
	private static int numberMethods = 0;
	
	@Override
	public void analyzeMethod(MethodDeclaration d) {
		numberMethods++;
		
		if( Modifier.isNative(d.getModifiers()) ) {
			numberNativeMethods++;
			System.out.println("Native method encountered: " + numberNativeMethods + " out of " +
					numberMethods + " total methods.");
		}
	}

	@Override
	public void afterAllMethods(ICompilationUnit compUnit,
			CompilationUnit rootNode) {
		System.out.println("Saw " + numberNativeMethods + " native methods out of " + numberMethods + 
				" total methods."); 
	}
	
	

}
