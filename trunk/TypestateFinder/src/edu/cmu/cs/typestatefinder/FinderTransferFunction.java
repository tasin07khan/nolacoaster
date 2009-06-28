package edu.cmu.cs.typestatefinder;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.BooleanLabel;
import edu.cmu.cs.crystal.flow.ILabel;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.LabeledResult;
import edu.cmu.cs.crystal.flow.SingleResult;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.tac.AbstractTACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.ITACAnalysisContext;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.ConstructorCallInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.DotClassInstruction;
import edu.cmu.cs.crystal.tac.model.EnhancedForConditionInstruction;
import edu.cmu.cs.crystal.tac.model.IVariableVisitor;
import edu.cmu.cs.crystal.tac.model.InstanceofInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.ReturnInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.SourceVariableReadInstruction;
import edu.cmu.cs.crystal.tac.model.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.SuperVariable;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.TypeVariable;
import edu.cmu.cs.crystal.tac.model.UnaryOperation;
import edu.cmu.cs.typestatefinder.TFLattice.InConditional;

import edu.cmu.cs.crystal.tac.*;

public class FinderTransferFunction extends
	AbstractTACBranchSensitiveTransferFunction<TFLattice> {



	@Override
	public TFLattice createEntryValue(MethodDeclaration method) {
		return new TFLattice();
	}

	private LatticeOps latticeOps = new LatticeOps();
	
	@Override
	public ILatticeOperations<TFLattice> getLatticeOperations() {
		return latticeOps;
	}

	@Override
	public IResult<TFLattice> transfer(BinaryOperation binop,
			List<ILabel> labels, TFLattice value) {
		if( value.isVarAField(binop.getOperand1()) || value.isVarAField(binop.getOperand2()) ) {
			value = value.addVarField(binop.getTarget());
			return resultFromLabels(value, labels, true);
		}
		else {
			return resultFromLabels(value, labels, false);
		}
	}

	private IResult<TFLattice> resultFromLabels(TFLattice lattice, List<ILabel> labels, boolean fieldChecked) {
		LabeledResult<TFLattice> result = LabeledResult.createResult(labels, lattice);
		
		for( ILabel label : labels ) {
			TFLattice to_add = lattice;
			if( label.equals(BooleanLabel.getBooleanLabel(true)) ) {
				to_add = to_add.branchOcurred(InConditional.TRUE);
				if( fieldChecked )
					to_add = to_add.fieldCheckOcurred();
			}
			else if( label.equals(BooleanLabel.getBooleanLabel(false)) ) {
				to_add = to_add.branchOcurred(InConditional.FALSE);
				if( fieldChecked )
					to_add = to_add.fieldCheckOcurred();	
			}
			result.put(label, to_add);
		}
		
		return result;
	}
	
	@Override
	public IResult<TFLattice> transfer(LoadFieldInstruction instr,
			List<ILabel> labels, TFLattice value) {
		boolean is_this =
		instr.getSourceObject().dispatch(new IVariableVisitor<Boolean>(){
			@Override public Boolean sourceVar(SourceVariable variable) { return false;	}
			@Override public Boolean superVar(SuperVariable variable) { return false; }
			@Override public Boolean tempVar(TempVariable variable) { return false;	}
			@Override public Boolean thisVar(ThisVariable variable) { return true; }
			@Override public Boolean typeVar(TypeVariable variable) { return false;	}
		});
		
		boolean is_field = false;
		
		if( is_this || value.isVarAField(instr.getSourceObject()) ) {
			value = value.addVarField(instr.getTarget());
			is_field = true;
		}
		
		if( labels.size() == 1 ) {
			return new SingleResult<TFLattice>(value);
		}
		
		return resultFromLabels(value, labels, is_field);
	}

	@Override
	public IResult<TFLattice> transfer(UnaryOperation unop,
			List<ILabel> labels, TFLattice value) {
		if( value.isVarAField(unop.getOperand()) ) {
			value = value.addVarField(unop.getTarget());
			return resultFromLabels(value, labels, true);
		}
		else {
			return resultFromLabels(value, labels, false);
		}
	}

	@Override
	public IResult<TFLattice> transfer(ArrayInitInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(CastInstruction instr,
			List<ILabel> labels, TFLattice value) {
		if( value.isVarAField(instr.getOperand()) ) {
			value = value.addVarField(instr.getTarget());
			return resultFromLabels(value, labels, true);
		}
		else {
			return resultFromLabels(value, labels, false);
		}
	}

	@Override
	public IResult<TFLattice> transfer(ConstructorCallInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(CopyInstruction instr,
			List<ILabel> labels, TFLattice value) {
		if( value.isVarAField(instr.getOperand()) ) {
			value = value.addVarField(instr.getTarget());
			return resultFromLabels(value, labels, true);
		}
		else {
			return resultFromLabels(value, labels, false);
		}
	}

	@Override
	public IResult<TFLattice> transfer(DotClassInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(EnhancedForConditionInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(InstanceofInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(LoadArrayInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(LoadLiteralInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(MethodCallInstruction instr,
			List<ILabel> labels, TFLattice value) {
		if( value.isVarAField(instr.getReceiverOperand()) ) {
			value = value.addVarField(instr.getTarget());
			return resultFromLabels(value, labels, true);
		}
		else {
			return resultFromLabels(value, labels, false);
		}
	}

	@Override
	public IResult<TFLattice> transfer(NewArrayInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(NewObjectInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(ReturnInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(SourceVariableDeclaration instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}
	
	@Override
	public IResult<TFLattice> transfer(SourceVariableReadInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(StoreArrayInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}

	@Override
	public IResult<TFLattice> transfer(StoreFieldInstruction instr,
			List<ILabel> labels, TFLattice value) {
		return resultFromLabels(value, labels, false);
	}
}
