package edu.cmu.cs.typestatefinder;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.crystal.flow.LatticeElement;
import edu.cmu.cs.crystal.tac.Variable;
import edu.rice.cs.plt.collect.CollectUtil;

/**
 * The TFLattice answers two questions: Does this variable hold a
 * field value or the result of a call to a field? Have we just performed
 * a branch based on the value of a field?
 * 
 * @author Nels E. Beckman
 */
public class TFLattice implements LatticeElement<TFLattice> {
	
	enum InConditional {
		NOT, TRUE, FALSE
	}
	
	private final boolean justCheckedField;

	private final Set<Variable> varIsField;
	
	private final InConditional isInConditional;
	
	public final static TFLattice BOTTOM = new TFLattice();
	
	public TFLattice() {
		this.varIsField = CollectUtil.emptySet();
		this.justCheckedField = false;
		this.isInConditional = InConditional.NOT;
	}
	
	public TFLattice(boolean jcf, Set<Variable> varIsField, InConditional isInConditional) {
		this.justCheckedField = jcf;
		this.varIsField = varIsField;
		this.isInConditional = isInConditional;
	}
	
	public boolean isVarAField(Variable v) {
		return this.varIsField.contains(v);
	}
	
	public boolean justCheckedField() {
		return this.justCheckedField;
	}
	
	public TFLattice addVarField(Variable v) {
		return new TFLattice(this.justCheckedField, CollectUtil.union(this.varIsField, v), this.isInConditional);
	}
	
	public TFLattice branchOcurred(InConditional branch) {
		return new TFLattice(this.justCheckedField, this.varIsField, branch);
	}
	
	public TFLattice fieldCheckOcurred() {
		return new TFLattice(true, this.varIsField, this.isInConditional);
	}
	
	@Override
	public boolean atLeastAsPrecise(TFLattice other, ASTNode node) {		
		if( this == BOTTOM ) {
			return true;
		}
		else if( other == BOTTOM ) {
			return false;
		}
		else if( !this.isInConditional.equals(other.isInConditional) ) {
			return false;
		}
		else if( this.justCheckedField == true ) {
			return varIsField.containsAll(other.varIsField);
		}
		else if( other.justCheckedField == true ) {
			return false;
		}
		else {
			return this.varIsField.containsAll(other.varIsField);
		}
	}

	@Override
	public TFLattice copy() {
		return this;
	}

	@Override
	public TFLattice join(TFLattice other, ASTNode node) {
		if( this == BOTTOM )
			return other;
		else if( other == BOTTOM )
			return this;
		
		boolean just_checked_field =
			this.isInConditional.equals(other.isInConditional) & this.justCheckedField & other.justCheckedField;
		
		InConditional is_in_conditional = 
			this.isInConditional.equals(other.isInConditional) ? this.isInConditional : InConditional.NOT;
		
		return new TFLattice(just_checked_field,
				CollectUtil.intersection(this.varIsField, other.varIsField),
				is_in_conditional);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (justCheckedField ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TFLattice other = (TFLattice) obj;
		if (justCheckedField != other.justCheckedField)
			return false;
		return true;
	}
}
