package edu.cmu.cs.typestatefinder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;

import edu.cmu.cs.crystal.internal.CrystalRuntimeException;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.ITACTransferFunction;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.util.Box;
import edu.cmu.cs.crystal.util.Option;

/**
 * A static analysis which will determine if a given expression
 * is the immediate result of a field of the current class. This
 * analysis is interesting because it attempts to find all of the
 * getter methods in a class, and includes those getter methods
 * in its analysis. Notes: This analysis only cares if the
 * field is a field of the current class, not other classes.
 * This class basically ignores getters defined in superclasses.
 * It's a must analysis, and therefore is precise but not 
 * sound.
 *  
 * @author Nels E. Beckman
 *
 */
final class MustBeAFieldAnalysis {

	private final Set<Getter> getters;
	private final TACFlowAnalysis<TupleLatticeElement<Variable, IsField>> flowAnalysis;
	
	static class IsField { 
		static final IsField Unknown = new IsField();
		static final IsField Bottom = new IsField();
		
		private final Option<IVariableBinding> field;
		
		IsField() { field = Option.none(); }
		IsField(IVariableBinding field) { this.field = Option.some(field); }
		Option<IVariableBinding> getField() { 
			assert( !this.equals(Unknown) && !this.equals(Bottom) );
			return field; 
		}
	}
	
	MustBeAFieldAnalysis(EntityWithMethods type, CompilationUnitTACs tacs) {
		this.getters = findGetters(type, tacs);
		ITACTransferFunction<TupleLatticeElement<Variable, IsField>> tf = new MustBeAFieldXferFunction(getters);
		this.flowAnalysis = 
			new TACFlowAnalysis<TupleLatticeElement<Variable,IsField>>(tf, tacs);
	}
	
	/**
	 * Does the given expression represent the value of a field of the current receiver?
	 */
	boolean isField(Expression expr) {
		IsField is_field = isFieldFromExpr(expr);
		return !is_field.equals(IsField.Bottom) &&
		        !is_field.equals(IsField.Unknown);
	}
	
	/** Which field does the given expression represent? Returns SOME only if it is known
	 *  exactly, and NONE in all other cases. */
	Option<IVariableBinding> whichField(Expression expr) {
		return isFieldFromExpr(expr).getField();
	}
	
	private IsField isFieldFromExpr(Expression expr) {
		TupleLatticeElement<Variable,IsField> tuple = flowAnalysis.getResultsAfter(expr);

		// If it actually is a field, I think we just have to do this...
		Variable expr_var;
		try {
			expr_var = flowAnalysis.getVariable(expr);
		} catch(CrystalRuntimeException cre) {
			return IsField.Unknown; // This is ultimately how the analysis fails, which
			// is really annoying.
		} catch(IllegalArgumentException iae) {
			return IsField.Unknown; // same issue
		}
		return tuple.get(expr_var);		
	}
	
	/**
	 * Finds getters in the given type. Getters must
	 * immediately return this.field. Static fields
	 * don't count.
	 */
	private static Set<Getter> findGetters(EntityWithMethods type, CompilationUnitTACs tacs) {
		Set<Getter> result = new HashSet<Getter>();
		for( final MethodDeclaration decl : type.getMethods() ) {
			final IsField is_field = isGetter(decl, tacs);
			if( !is_field.equals(IsField.Unknown) && !is_field.equals(IsField.Bottom) ) {
				result.add(new Getter.GetterImpl(decl.resolveBinding(), is_field.getField()));
			}
		}
		return result;
	}

	// TODO: We could make the whole process go until fixed-point, and that way we
	// could have getters of getters...
	private static IsField isGetter(MethodDeclaration decl, CompilationUnitTACs tacs) {
		final Box<IsField> is_getter = new Box<IsField>(IsField.Unknown);
		// If the method returns void, then it can't be a getter!
		if( isVoid(decl.resolveBinding().getReturnType()) ) return IsField.Unknown;
		
		final TACFlowAnalysis<TupleLatticeElement<Variable,IsField>> flowAnalysis = 
			new TACFlowAnalysis<TupleLatticeElement<Variable,IsField>>(
					new MustBeAFieldXferFunction(Collections.<Getter>emptySet()),tacs);
		// If the method sees several return statements, we only return the last one.
		decl.accept(new ASTVisitor() {
			@Override public boolean visit(AnonymousClassDeclaration acd) { return false; }
			
			@Override public void endVisit(ReturnStatement node) {
				Expression return_expr = node.getExpression();
				assert(return_expr != null);
				IsField is_field =
					flowAnalysis.getResultsAfter(return_expr).get(flowAnalysis.getVariable(return_expr));
				is_getter.setValue(is_field);
			}
		});
		return is_getter.getValue();
	}

	private static boolean isVoid(ITypeBinding t) {
		if(!t.isPrimitive())
			return false;
		return "void".equals(t.getName());
	}
}
