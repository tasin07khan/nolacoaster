package edu.cmu.cs.typestatefinder;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.ITACAnalysisContext;
import edu.cmu.cs.crystal.tac.ITACTransferFunction;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.ConstructorCallInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.DotClassInstruction;
import edu.cmu.cs.crystal.tac.model.EnhancedForConditionInstruction;
import edu.cmu.cs.crystal.tac.model.InstanceofInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.ReturnInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.SourceVariableReadInstruction;
import edu.cmu.cs.crystal.tac.model.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.UnaryOperation;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.typestatefinder.MustBeAFieldAnalysis.IsField;

class MustBeAFieldXferFunction implements ITACTransferFunction<TupleLatticeElement<Variable, IsField>> {

	private final Set<IMethodBinding> getters;
	
	private ITACAnalysisContext context;
	
	/**
	 * Lattice operations, defined in-line on the IsField lattice element.
	 */
	private final TupleLatticeOperations<Variable, IsField> latticeOps = 
		new TupleLatticeOperations<Variable, IsField>(new ILatticeOperations<IsField>(){
			@Override public boolean atLeastAsPrecise(IsField info, IsField reference, ASTNode node) {
				switch(reference) {
				case Yes: case Bottom: // Fall-through.
					return info.equals(reference);
				case Unknown: return true;
				default: throw new RuntimeException("Impossible");
				}
			}

			@Override public IsField bottom() { return IsField.Bottom; }
			@Override public IsField copy(IsField original) { return original; }

			@Override
			public IsField join(IsField someInfo, IsField otherInfo, ASTNode node) {
				switch(otherInfo) {
				case Yes: case Bottom: // Fall-through.
					return someInfo.equals(otherInfo) ? someInfo : IsField.Unknown;
				case Unknown: return IsField.Unknown;
				default: throw new RuntimeException("Impossible");
				}
			}}, 
				IsField.Unknown);
	
	public MustBeAFieldXferFunction(Set<IMethodBinding> getters) {
		this.getters = getters;
	}

	@Override
	public void setAnalysisContext(ITACAnalysisContext context) {
		this.context = context;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			ArrayInitInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			BinaryOperation binop, TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			CastInstruction instr, TupleLatticeElement<Variable, IsField> value) {
		IsField is_field = value.get(instr.getOperand());
		value.put(instr.getTarget(), is_field);
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			DotClassInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			ConstructorCallInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			CopyInstruction instr, TupleLatticeElement<Variable, IsField> value) {
		IsField is_field = value.get(instr.getOperand());
		value.put(instr.getTarget(), is_field);
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			EnhancedForConditionInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			InstanceofInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			LoadLiteralInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			LoadArrayInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		// We say that elements loaded from arrays are not
		// themselves fields, even if the array was a field.
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			LoadFieldInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		if( instr.getSourceObject().equals(this.context.getThisVariable()) ) {
			value.put(instr.getTarget(), IsField.Yes);
		}
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			MethodCallInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		if( this.getters.contains(instr.resolveBinding()) ) {
			value.put(instr.getTarget(), IsField.Yes);
		}
		// Default is unknown, so I don't need to put it in for this
		// brand-new variable.
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			NewArrayInstruction instr, TupleLatticeElement<Variable, IsField> value) { return value; }

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			NewObjectInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			ReturnInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			StoreArrayInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			StoreFieldInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			SourceVariableDeclaration instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(
			SourceVariableReadInstruction instr,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> transfer(UnaryOperation unop,
			TupleLatticeElement<Variable, IsField> value) {
		return value;
	}

	@Override
	public TupleLatticeElement<Variable, IsField> createEntryValue(
			MethodDeclaration method) {
		return this.latticeOps.getDefault();
	}

	@Override
	public AnalysisDirection getAnalysisDirection() {
		return AnalysisDirection.FORWARD_ANALYSIS;	
	}

	@Override
	public ILatticeOperations<TupleLatticeElement<Variable, IsField>> getLatticeOperations() {
		return this.latticeOps;
	}
}