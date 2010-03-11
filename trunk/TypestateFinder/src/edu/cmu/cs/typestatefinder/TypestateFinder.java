package edu.cmu.cs.typestatefinder;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.util.Box;

public class TypestateFinder extends AbstractCompilationUnitAnalysis {

	/** Remove a string signature for the given method. This string signature is 
	 *  unique for a method, and does not overlap with methods that is overloads. */
	public static String methodSig(IMethodBinding method, String type_name) {
		StringBuilder result = new StringBuilder(type_name);
		result.append(".").append(method.getName()).append("(");
		for( ITypeBinding param_type : method.getParameterTypes() ) {
			result.append(param_type.getQualifiedName()).append(";");
		}
		return result.append(")").toString();
	}
	
	/** Given a type name with static arguments, removes the static arguments. */
	public static String removeStaticArgs(String type_name) {
		if( !type_name.contains("<") ) return type_name;
		
		return type_name.substring(0, type_name.indexOf('<'));
	}
	
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
		
		private IMethodBinding getCurrentMethod(ASTNode node) {
			ASTNode current;
			for( current = node.getParent(); !(current instanceof MethodDeclaration); current = current.getParent() ) {
				if( current == null ) 
					throw new IllegalArgumentException("Node not inside a method: " + node);
			}
			return ((MethodDeclaration)current).resolveBinding();
		}
		
		private String accessibility(int modifier) {
			if( Modifier.isPrivate(modifier) ) {
				return "private";
			}
			else if( Modifier.isProtected(modifier) ) {
				return "protected";
			}
			else if( Modifier.isPublic(modifier) ) {
				return "public";
			}
			else {
				return "default";
			}
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
					
					class_name = removeStaticArgs(class_name);
					
					// We want method name and method accessibility
					IMethodBinding current_method = getCurrentMethod(node);
					String output = "TypestateFinder: " + cu.getPackage().getName() + ", " + resource.getName() + ", " +
						cu.getLineNumber(node.getStartPosition()) + ", " + class_name + ", " +
						methodSig(current_method, class_name) + ", " +
						accessibility(current_method.getModifiers());
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
				// NON GETTER VERSION
//				@Override
//                public void endVisit(FieldAccess node) {
//                        if( node.getExpression() instanceof ThisExpression ) {
//                                saw_field.setValue(true);
//                        }
//                        super.endVisit(node);
//                }
//                @Override
//                public void endVisit(SimpleName node) {
//                        // Easiest case, just see if this is a field. If so, it must be of 'this.'
//                        if( node.getParent() instanceof QualifiedName )
//                                return;
//                        
//                        IBinding binding = node.resolveBinding();
//                        if( binding.getKind() == IBinding.VARIABLE ) {
//                                IVariableBinding var_bind = (IVariableBinding)binding;
//                                if( !var_bind.isEnumConstant() && var_bind.isField() && !Modifier.isStatic(var_bind.getModifiers()) ) {
//                                        saw_field.setValue(true);
//                                }
//                        }
//                }
                // GETTER VERSION
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