package edu.cmu.cs.typestatefinder;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;

/**
 * From a file listing all the classes that define typestate, list all
 * of the classes that use typestate, and the number of types that methods
 * of those classes were called.
 * 
 * @author Nels E. Beckman
 *
 */
public final class TypestateUsageFinder extends AbstractCrystalMethodAnalysis {

	public TypestateUsageFinder() {
		super();
	}

	@Override
	public void analyzeMethod(MethodDeclaration d) {
		// TODO Auto-generated method stub

	}

}
