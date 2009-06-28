package edu.cmu.cs.typestatefinder;

import edu.cmu.cs.crystal.simple.SimpleLatticeOperations;
import edu.cmu.cs.typestatefinder.TFLattice.InConditional;
import edu.rice.cs.plt.collect.CollectUtil;

public class LatticeOps extends SimpleLatticeOperations<TFLattice> {

	@Override
	public boolean atLeastAsPrecise(TFLattice left, TFLattice right) {
		if( left == bottom() ) {
			return true;
		}
		else if( right == bottom() ) {
			return false;
		}
		else if( !left.isInConditional().equals(right.isInConditional()) ) {
			return false;
		}
		else if( left.justCheckedField() == true ) {
			return left.varIsField().containsAll(right.varIsField());
		}
		else if( right.justCheckedField() == true ) {
			return false;
		}
		else {
			return left.varIsField().containsAll(right.varIsField());
		}
	}
	
	private final static TFLattice BOTTOM = new TFLattice();
	
	@Override
	public TFLattice bottom() {
		return BOTTOM;
	}

	@Override
	public TFLattice copy(TFLattice original) {
		return original;
	}

	@Override
	public TFLattice join(TFLattice left, TFLattice right) {
		if( left == bottom() )
			return right;
		else if( right == bottom() )
			return left;
		
		boolean just_checked_field =
			left.isInConditional().equals(right.isInConditional()) & left.justCheckedField() & right.justCheckedField();
		
		InConditional is_in_conditional = 
			left.isInConditional().equals(right.isInConditional()) ? left.isInConditional() : InConditional.NOT;
		
		return new TFLattice(just_checked_field,
				CollectUtil.intersection(left.varIsField(), right.varIsField()),
				is_in_conditional);
	}
}
