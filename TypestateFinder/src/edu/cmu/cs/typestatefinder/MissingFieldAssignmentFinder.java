package edu.cmu.cs.typestatefinder;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.util.Option;
import edu.cmu.cs.crystal.util.Pair;

/**
 * A protocol detector that attempts to find initialization-style
 * protocols by looking for classes that never assign values to
 * some of its fields in some of its constructors. For now, let's
 * just do this on a class-by-class basis, and ignore protected
 * fields from superclasses. This class is not flow sensitive,
 * although this means that there is a possibility for false-
 * negatives.
 * 
 * @author Nels E. Beckman
 */
public final class MissingFieldAssignmentFinder extends
		AbstractCompilationUnitAnalysis {

	@Override
	public void analyzeCompilationUnit(CompilationUnit d) {
		ClassVisitor visitor = new ClassVisitor();
		d.accept(visitor);
	}

	// In the given list, find the fields that are uninitialized by the declaration itself.
	private static Set<IVariableBinding> findUninitializedFields(FieldDeclaration[] fields) {
		Set<IVariableBinding> result = new HashSet<IVariableBinding>();
		
		for( FieldDeclaration field : fields ) {
			// We only care about instance fields.
			if( Modifier.isStatic(field.getModifiers()) ) continue;
			// Also, if it's final, then there can't really be a protocol
			if( Modifier.isFinal(field.getModifiers()) ) continue;
			
			// No initializer or an initializer but the value is the literal null
			for( Object var_decl_ : field.fragments() ) {
				VariableDeclarationFragment var_decl = (VariableDeclarationFragment)var_decl_;
				
				// Primitive fields cannot be null
				if( var_decl.resolveBinding().getType().isPrimitive() ) continue;
				// Also, I don't care about arrays, even though maybe I should.
				if( var_decl.resolveBinding().getType().isArray() ) continue;
				
				Expression init = var_decl.getInitializer();
				
				if( init == null ) result.add(var_decl.resolveBinding());
				else if( init.resolveTypeBinding().isNullType() ) result.add(var_decl.resolveBinding());
			}
		}
		
		return result;
	}
	
	private class ClassVisitor extends ASTVisitor {

		@Override
		public void endVisit(TypeDeclaration node) {
			if( node.isInterface() ) return; // classes only...
			
			// Get all of the fields, remove the ones
			// that have field initializers.
			Set<IVariableBinding> unitialized = findUninitializedFields(node.getFields());
			
			// Accept constructor visitor
			Pair<Set<IVariableBinding>, Boolean> visitor_result = 
				ConstructorsVisitor.findUnassigned(node, unitialized);
			Set<IVariableBinding> unassigned_vars = visitor_result.fst(); 
			Boolean saw_sync = visitor_result.snd();
			
			// See if any of those fields ever have methods called on them.
			Set<IVariableBinding> called_unassigned = findUnassignedCalledVars(node, unassigned_vars);
			
			// See if remaining fields exist
			if( !called_unassigned.isEmpty() ) {
				String error_msg = "There are unassigned fields in one of the constructors. Fields are " +
					called_unassigned.toString();
				reporter.reportUserProblem(error_msg, node, getName());
				
				// Make sure you print all of the data we needed for the original typestate finder
				String class_name = node.resolveBinding().getQualifiedName();
				String msg = "NullFieldClass, " + class_name +", " + saw_sync.toString();
				System.out.println(msg);
			}
		}
		
		private Set<IVariableBinding> findUnassignedCalledVars(TypeDeclaration node, 
				final Set<IVariableBinding> unassignedVars) {
			final MustBeAFieldAnalysis analysis = 
				new MustBeAFieldAnalysis(EntityWithMethods.Util.fromTypeDeclaration(node), getInput().getComUnitTACs().unwrap());
			final Set<IVariableBinding> result = new HashSet<IVariableBinding>();
			for( MethodDeclaration method : node.getMethods() ) {
				method.accept(new ASTVisitor() {
					@Override
					public void endVisit(MethodInvocation node) {
						if( node.getExpression() != null && analysis.isField(node.getExpression())) {
							Option<IVariableBinding> var_ = analysis.whichField(node.getExpression());
							if( var_.isSome() && unassignedVars.contains(var_.unwrap()) ) {
								result.add(var_.unwrap());
							}
						}
					}

					@Override public boolean visit(AnonymousClassDeclaration node) { return false; }
				});
			}
			return result;
		}
	}
	
	private static class SingleConstructorVisitor extends ASTVisitor {
		private final Set<IVariableBinding> uninitialized;
		// map is mutated during visiting.
		private final Map<IMethodBinding, Set<IVariableBinding>> varsUnassignedInCxtr;
		
		private final IMethodBinding thisConstructor;
		
		public SingleConstructorVisitor(
				Set<IVariableBinding> unitialized,
				Map<IMethodBinding, Set<IVariableBinding>> varsUnassignedInCxtr,
				IMethodBinding thisConstructor) {
			this.uninitialized = unitialized;
			this.varsUnassignedInCxtr = varsUnassignedInCxtr;
			this.thisConstructor = thisConstructor;
		}

		@Override
		public void endVisit(Assignment node) {
			// Only counts if right-hand side is not null!
			if( node.getRightHandSide().resolveTypeBinding().isNullType() ) return;
			
			// If LHS is a field, remove it
			node.getLeftHandSide().accept(new ASTVisitor(){
				@Override
				public boolean visit(FieldAccess node) {
					if( uninitialized.contains(node.resolveFieldBinding()) ) {
						removeBinding(node.resolveFieldBinding());
					}
					return false;
				}

				@Override
				public boolean visit(SimpleName node) {
					if( node.resolveBinding() instanceof IVariableBinding ) {
						IVariableBinding var_binding = (IVariableBinding)node.resolveBinding();
						if( var_binding.isField() && uninitialized.contains(var_binding) ) {
							removeBinding(var_binding);
						}
					}
					return false;
				}
				private void removeBinding(IVariableBinding field) {
					assert(varsUnassignedInCxtr.containsKey(thisConstructor));
					SingleConstructorVisitor.this.varsUnassignedInCxtr.get(thisConstructor).remove(field);
				}
			});
		}

		@Override
		public void endVisit(ConstructorInvocation node) {
			// Now because we are calling another constructor, we get to remove,
			// for free, everything that they remove.
			IMethodBinding xtr_binding = node.resolveConstructorBinding();
			if( varsUnassignedInCxtr.containsKey(xtr_binding) ) {
				// Take uninitialized - other xtr. We can remove that set from here.
				Set<IVariableBinding> to_remove = new HashSet<IVariableBinding>(uninitialized);
				to_remove.removeAll(varsUnassignedInCxtr.get(xtr_binding));
				// now remove
				varsUnassignedInCxtr.get(thisConstructor).removeAll(to_remove);
			}
		}
	}
	
	// The constructor actually does a sort of worklist thing.
	private static class ConstructorsVisitor extends ASTVisitor {
		private final Set<IVariableBinding> unitialized;
		// map is mutated during visiting.
		private final Map<IMethodBinding, Set<IVariableBinding>> varsUnassignedInCxtr;
		// did we see a synchronized method?
		boolean sawSync = false;
		
		private ConstructorsVisitor(Set<IVariableBinding> unitialized,
				Map<IMethodBinding, Set<IVariableBinding>> varsUnassigned) {
			this.unitialized = Collections.unmodifiableSet(new HashSet<IVariableBinding>(unitialized));
			// map is mutated during visiting.
			this.varsUnassignedInCxtr = new HashMap<IMethodBinding, Set<IVariableBinding>>();
			// gotta do a stupid deep copy...
			for( Map.Entry<IMethodBinding, Set<IVariableBinding>> entry : varsUnassigned.entrySet() ) {
				this.varsUnassignedInCxtr.put(entry.getKey(), new HashSet<IVariableBinding>(entry.getValue()) );
			}
		}

		@Override
		public void endVisit(MethodDeclaration node) {
			this.sawSync |= Modifier.isSynchronized(node.getModifiers());
			
			IMethodBinding resolvedBinding = node.resolveBinding();
			if( resolvedBinding.isConstructor() ) {
				// Add this binding to the map
				// If map does not contain this constructor entry, add all uninit-ed vars.
				// Otherwise, leave it alone, since it represents the results of a previous
				// iteration.
				if( !varsUnassignedInCxtr.containsKey(resolvedBinding) ) {
					varsUnassignedInCxtr.put(resolvedBinding, new HashSet<IVariableBinding>(unitialized));
				}
				// This single-constructor visitor modifies the map in-place.
				node.accept(new SingleConstructorVisitor(unitialized, varsUnassignedInCxtr, resolvedBinding));
			}
		}



		public static Pair<Set<IVariableBinding>, Boolean> findUnassigned(ASTNode node, 
				Set<IVariableBinding> unitialized) {
			Map<IMethodBinding, Set<IVariableBinding>> last_map = Collections.emptyMap();
			// perform worklist, comparing to last_map and visiting until they are
			// the same.
			boolean continue_;
			ConstructorsVisitor visitor;
			do {
				visitor = new ConstructorsVisitor(unitialized, last_map);
				node.accept(visitor);
				continue_ = !visitor.varsUnassignedInCxtr.equals(last_map);
				last_map = visitor.varsUnassignedInCxtr;
			} while( continue_ );
			
			if( last_map.isEmpty() ) {
				// This means there were no constructors at all.
				return Pair.create(unitialized, visitor.sawSync);
			}
			
			// Gather together all of the fields that were not assigned in at least on xtr
			Set<IVariableBinding> all_possible_nulls = new HashSet<IVariableBinding>();
			for( Set<IVariableBinding> possible_nulls : last_map.values() ) {
				all_possible_nulls.addAll(possible_nulls);
			}
			
			if( all_possible_nulls.isEmpty() ) {
				return Pair.create(Collections.<IVariableBinding>emptySet(), Boolean.FALSE);
			}
			else {
				// XXX What we'd like to do now is go through each of these fields and see
				//     if any method is ever called on them. To do that I am going to need to
				//     fix up my field finder.
				return Pair.create(all_possible_nulls, visitor.sawSync);
			}			
		}
	}	
}