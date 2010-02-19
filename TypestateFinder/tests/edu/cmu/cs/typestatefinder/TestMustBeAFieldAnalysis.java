package edu.cmu.cs.typestatefinder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;

public final class TestMustBeAFieldAnalysis extends
		AbstractCompilationUnitAnalysis {

	@Override
	public void analyzeCompilationUnit(CompilationUnit d) {
		d.accept(new ASTVisitor() {
			@Override
			public boolean visit(TypeDeclaration node) {
				ExprVisitor.lookForFields(EntityWithMethods.Util.fromTypeDeclaration(node), 
						getInput().getComUnitTACs().unwrap(), getReporter());
				return false;
			}
		});
	}

	private static class ExprVisitor extends ASTVisitor {
		private final MustBeAFieldAnalysis analysis;
		private final CompilationUnitTACs tacs;
		private final IAnalysisReporter reporter;
		
		ExprVisitor(EntityWithMethods type, CompilationUnitTACs tacs, IAnalysisReporter reporter) {
			this.analysis = new MustBeAFieldAnalysis(type, tacs);
			this.tacs = tacs;
			this.reporter = reporter;
		}

		static void lookForFields(EntityWithMethods type, 
				CompilationUnitTACs tacs, IAnalysisReporter reporter) {
			ExprVisitor visitor = new ExprVisitor(type, tacs, reporter);
			for( MethodDeclaration method : type.getMethods() ) {
				if( method.getBody() != null ) 
					method.getBody().accept(visitor);
			}
		}
		
		@Override
		public boolean visit(MethodInvocation node) {
			if( node.getExpression() != null )
				node.getExpression().accept(this);
			return false;
		}

		@Override
		public boolean visit(FieldAccess node) {
			node.getExpression().accept(this);
			return false;
		}

		@Override
		public boolean visit(Assignment node) {
			// The left hand side may be an expression, but it doesn't
			// have a variable in Crystal, so I have to skip it...
			node.getRightHandSide().accept(this);
			return false;
		}

		@Override
		public boolean visit(QualifiedName node) {
			return false;
		}

		@Override
		public void postVisit(ASTNode node) {
			if( node instanceof Expression ) {
				Expression expr = (Expression)node;
				if( this.analysis.isField(expr) ) {
					reporter.reportUserProblem("This is a field.", node, "TestMustBeAFieldAnalysis");
				}
			}
		}

		@Override
		public boolean visit(AnonymousClassDeclaration node) {
			lookForFields(EntityWithMethods.Util.fromAnonymousClass(node), tacs, reporter);
			return false;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			lookForFields(EntityWithMethods.Util.fromTypeDeclaration(node), tacs, reporter);
			return false;
		}
	}
	
}
