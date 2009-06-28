package edu.cmu.cs.typestatefinder;

import java.lang.reflect.Modifier;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.util.Box;

public class TypestateFinder extends AbstractCrystalMethodAnalysis {

	@Override
	public void analyzeMethod(MethodDeclaration d) {
		TACFlowAnalysis<TFLattice> analysis =
		new TACFlowAnalysis<TFLattice>(new FinderTransferFunction(),
				this.analysisInput.getComUnitTACs().unwrap());
		
		//d.accept(new FinderVisitor(analysis));
		d.accept(new PartialFunctionFinder());
	}

	private class FinderVisitor extends ASTVisitor {
		private final TACFlowAnalysis<TFLattice> analysis;
		
		FinderVisitor(TACFlowAnalysis<TFLattice> analysis) {
			this.analysis = analysis;
		}

		@Override
		public void endVisit(ThrowStatement node) {
			TFLattice lattice = analysis.getResultsBefore(node);
			if( lattice.justCheckedField() ) {
				TypestateFinder.this.reporter.reportUserProblem("Possible typestate", 
						node, 
						TypestateFinder.this.getName());
			}
		}
	}
	
	/**
	 * Finds methods that are partial functions of 'this.' 
	 * 
	 * @author Nels E. Beckman
	 */
	private class PartialFunctionFinder extends ASTVisitor {
		
		private final boolean checkExistsAboveUs;
		
		PartialFunctionFinder() {
			this(false);
		}
		
		private PartialFunctionFinder(boolean checkExistsAboveUs) {
			this.checkExistsAboveUs = checkExistsAboveUs;
		}
		
		@Override
		public void endVisit(ThrowStatement node) {
			if( this.checkExistsAboveUs ) {
				TypestateFinder.this.reporter.reportUserProblem("Possible typestate", 
						node, 
						TypestateFinder.this.getName());
			}
		}

		
		
		@Override
		public boolean visit(CatchClause node) {
			// Visiting a catch clause really should reset the 'existsAboveUs' since it's
			// not directly in the execution path.
			node.getException().accept(this);
			node.getBody().accept(new PartialFunctionFinder(false));
			return false;
		}

		@Override
		public boolean visit(IfStatement node) {
			Expression conditional_expr = node.getExpression();
			Statement then = node.getThenStatement();
			Statement else_branch = node.getElseStatement();
			
			// See if there is a field access in the conditional.
			final Box<Boolean> saw_field = Box.box(false);
			conditional_expr.accept(new PartialFunctionFinder(checkExistsAboveUs){
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

//				@Override
//				public void endVisit(QualifiedName node) {
//					// TODO Auto-generated method stub
//					super.endVisit(node);
//				}
//
//				@Override
//				public void endVisit(ThisExpression node) {
//					// TODO Auto-generated method stub
//					super.endVisit(node);
//				}
			});
			
			// If we just saw a field... or if we already saw one
			final PartialFunctionFinder next_visitor = 
				new PartialFunctionFinder(checkExistsAboveUs | saw_field.getValue());
			then.accept(next_visitor);
			
			if( else_branch != null )
				else_branch.accept(next_visitor);
			
			// We handle the recursion ourselves.
			return false;
		}
	}
}
