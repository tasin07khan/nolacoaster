package edu.cmu.cs.typestatefinder;

import java.lang.reflect.Modifier;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.util.Box;

public class TypestateFinder extends AbstractCrystalMethodAnalysis {
	
	@Override
	public void analyzeMethod(MethodDeclaration d) {
		d.accept(new PartialFunctionFinder(d.resolveBinding().getDeclaringClass()));
	}
	
	/**
	 * Finds methods that are partial functions of 'this.' 
	 * 
	 * @author Nels E. Beckman
	 */
	private class PartialFunctionFinder extends ASTVisitor {
		
		private final boolean checkExistsAboveUs;
		
		private final ITypeBinding definingClass;
		
		PartialFunctionFinder(ITypeBinding definingClass) {
			this(false, definingClass);
		}
		
		private PartialFunctionFinder(boolean checkExistsAboveUs, ITypeBinding definingClass) {
			this.checkExistsAboveUs = checkExistsAboveUs;
			this.definingClass = definingClass;
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
					
					String output = cu.getPackage().getName() + ", " + resource.getName() + ", " +
						cu.getLineNumber(node.getStartPosition()) + ", " + this.definingClass.getQualifiedName();
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
			node.getBody().accept(new PartialFunctionFinder(false, this.definingClass));
			return false;
		}

		@Override
		public boolean visit(IfStatement node) {
			Expression conditional_expr = node.getExpression();
			Statement then = node.getThenStatement();
			Statement else_branch = node.getElseStatement();
			
			// See if there is a field access in the conditional.
			final Box<Boolean> saw_field = Box.box(false);
			conditional_expr.accept(new PartialFunctionFinder(checkExistsAboveUs, this.definingClass){
				// If we see a field, record that fact...
				
				@Override
				public void endVisit(FieldAccess node) {
					if( node.getExpression() instanceof ThisExpression ) {
						saw_field.setValue(true);
					}
					super.endVisit(node);
				}

				@Override
				public void endVisit(SimpleName node) {
					// Easiest case, just see if this is a field. If so, it must be of 'this.'
					if( node.getParent() instanceof QualifiedName )
						return;
					
					IBinding binding = node.resolveBinding();
					if( binding.getKind() == IBinding.VARIABLE ) {
						IVariableBinding var_bind = (IVariableBinding)binding;
						if( !var_bind.isEnumConstant() && var_bind.isField() && !Modifier.isStatic(var_bind.getModifiers()) ) {
							saw_field.setValue(true);
						}
					}
				}
			});
			
			// If we just saw a field... or if we already saw one
			final PartialFunctionFinder next_visitor = 
				new PartialFunctionFinder(checkExistsAboveUs | saw_field.getValue(), this.definingClass);
			then.accept(next_visitor);
			
			if( else_branch != null )
				else_branch.accept(next_visitor);
			
			// We handle the recursion ourselves.
			return false;
		}
	}
}
