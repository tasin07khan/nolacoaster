package edu.cmu.cs.typestatefinder;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.util.Option;

/**
 * A 'getter' is a method binding, and a binding to the
 * variable that is returned, if known.
 * @author Nels E. Beckman
 *
 */
public interface Getter {
	public IMethodBinding getterMethod();
	/**
	 * Field returned by this getter, if known.
	 */
	public Option<IVariableBinding> getterVariable();
	
	static class GetterImpl implements Getter {
		private final IMethodBinding method;
		private final Option<IVariableBinding> varBinding;
		
		public GetterImpl(IMethodBinding method,
				Option<IVariableBinding> varBinding) {
			super();
			this.method = method;
			this.varBinding = varBinding;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((method == null) ? 0 : method.hashCode());
			result = prime * result
					+ ((varBinding == null) ? 0 : varBinding.hashCode());
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
			GetterImpl other = (GetterImpl) obj;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			if (varBinding == null) {
				if (other.varBinding != null)
					return false;
			} else if (!varBinding.equals(other.varBinding))
				return false;
			return true;
		}

		@Override
		public IMethodBinding getterMethod() {
			return this.method;
		}

		@Override
		public Option<IVariableBinding> getterVariable() {
			return this.varBinding;
		}
	}
}
