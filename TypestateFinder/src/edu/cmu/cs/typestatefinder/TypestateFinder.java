package edu.cmu.cs.typestatefinder;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.util.Box;

public class TypestateFinder extends AbstractCompilationUnitAnalysis {

	@Override
	public void analyzeCompilationUnit(CompilationUnit d) {
		d.accept(new ASTVisitor() {
			@Override
			public void endVisit(TypeDeclaration node) {
				System.out.println("Starting: " + node.resolveBinding().getQualifiedName());
				node.accept(new PartialFunctionFinder(EntityWithMethods.Util.fromTypeDeclaration(node)));
			}
		});
	}
	
	/**
	 * Finds methods that are partial functions of 'this.' 
	 * 
	 * @author Nels E. Beckman
	 */
	private class PartialFunctionFinder extends ASTVisitor {
		
		private final boolean checkExistsAboveUs;
		
		private final ITypeBinding definingClassBinding;
		
		private final MustBeAFieldAnalysis fieldAnalysis;
		
		PartialFunctionFinder(EntityWithMethods definingClass) {
			this(false, definingClass);
		}
		
		private PartialFunctionFinder(boolean checkExistsAboveUs, EntityWithMethods definingClass) {
			this(checkExistsAboveUs, definingClass.getType() ,new MustBeAFieldAnalysis(definingClass, getInput().getComUnitTACs().unwrap()));
		}
		
		private PartialFunctionFinder(boolean checkExistsAboveUs, ITypeBinding definingClass, MustBeAFieldAnalysis fieldAnalysis) {
			this.checkExistsAboveUs = checkExistsAboveUs;
			this.definingClassBinding = definingClass;
			this.fieldAnalysis = fieldAnalysis;
		}
		
		@Override
		public void endVisit(ThrowStatement node) {
			if( this.checkExistsAboveUs ) {
				TypestateFinder.this.reporter.reportUserProblem("Possible typestate", 
						node, 
						TypestateFinder.this.getName());

				// Identify the closest resource to the ASTNode,
				ASTNode root = node.getRoot();
				IResource resource;
				if (root.getNodeType() == ASTNode.COMPILATION_UNIT) {
					CompilationUnit cu = (CompilationUnit) root;
					IJavaElement je = cu.getJavaElement();
					resource = je.getResource();
					cu.getPackage();
					
					String class_name;
					if( this.definingClassBinding.isAnonymous() ) {
						// Guess we should print the class that is being instantiated...
						if( this.definingClassBinding.getInterfaces().length > 0 ) {
							assert(this.definingClassBinding.getInterfaces().length == 1);
							class_name = this.definingClassBinding.getInterfaces()[0].getQualifiedName();
						} else {
							class_name = this.definingClassBinding.getSuperclass().getQualifiedName();
						}
					} else if( this.definingClassBinding.isLocal() ) {
						// A local, non-anonymous class actually presents some difficulties.
						// Just print out the fact that it is local...
						class_name = "LOCAL";
					} else {
						class_name = this.definingClassBinding.getQualifiedName();
					}
					
					String output = cu.getPackage().getName() + ", " + resource.getName() + ", " +
						cu.getLineNumber(node.getStartPosition()) + ", " + class_name;
					System.out.println(output);
				}
				else {
					// Use the high-level Workspace
					System.err.println("Problem: No compilation unit.");
				}
			}
		}

		
		
		@Override
		public boolean visit(CatchClause node) {
			// Visiting a catch clause really should reset the 'existsAboveUs' since it's
			// not directly in the execution path.
			node.getException().accept(this);
			node.getBody().accept(new PartialFunctionFinder(false, this.definingClassBinding, this.fieldAnalysis));
			return false;
		}

		@Override
		public boolean visit(IfStatement node) {
			Expression conditional_expr = node.getExpression();
			Statement then = node.getThenStatement();
			Statement else_branch = node.getElseStatement();
			
			// See if there is a field access in the conditional.
			final Box<Boolean> saw_field = Box.box(Boolean.FALSE);
			conditional_expr.accept(new PartialFunctionFinder(checkExistsAboveUs, this.definingClassBinding, this.fieldAnalysis){
				// If we see a field, record that fact...
				
				@Override
				public void postVisit(ASTNode node) {
					if( node instanceof Expression ) {
						Expression expr = (Expression)node;
						if( fieldAnalysis.isField(expr) ) {
							saw_field.setValue(Boolean.TRUE);
						}
					}
				}
			});
			
			// If we just saw a field... or if we already saw one
			final PartialFunctionFinder next_visitor = 
				new PartialFunctionFinder(checkExistsAboveUs | saw_field.getValue(), this.definingClassBinding, this.fieldAnalysis);
			then.accept(next_visitor);
			
			if( else_branch != null )
				else_branch.accept(next_visitor);
			
			// We handle the recursion ourselves.
			return false;
		}

		private void recur(EntityWithMethods type) {
			ASTVisitor visitor = new PartialFunctionFinder(type);
			for( MethodDeclaration method : type.getMethods() ) {
				if( method.getBody() != null  )
					method.getBody().accept(visitor);
			}
		}
		
		@Override
		public boolean visit(AnonymousClassDeclaration node) {
			recur(EntityWithMethods.Util.fromAnonymousClass(node));
			return false;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			recur(EntityWithMethods.Util.fromTypeDeclaration(node));
			return false;
		}		
	}
}