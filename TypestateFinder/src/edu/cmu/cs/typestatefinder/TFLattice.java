package edu.cmu.cs.typestatefinder;

import java.util.Collections;
import java.util.Set;

import edu.cmu.cs.crystal.tac.model.Variable;
import edu.rice.cs.plt.collect.CollectUtil;

/**
 * The TFLattice answers two questions: Does this variable hold a
 * field value or the result of a call to a field? Have we just performed
 * a branch based on the value of a field?
 * 
 * @author Nels E. Beckman
 */
public class TFLattice {
	
	enum InConditional {
		NOT, TRUE, FALSE
	}
	
	private final boolean justCheckedField;

	private final Set<Variable> varIsField;
	
	private final InConditional isInConditional;
	
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

	InConditional isInConditional() {
		return this.isInConditional;
	}

	Set<Variable> varIsField() {
		return Collections.unmodifiableSet(this.varIsField);
	}
}
