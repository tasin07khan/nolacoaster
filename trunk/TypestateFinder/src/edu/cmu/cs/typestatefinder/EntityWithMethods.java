package edu.cmu.cs.typestatefinder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * A type for things that have methods.
 * 
 * @author Nels E. Beckman
 *
 */
public interface EntityWithMethods {
	MethodDeclaration[] getMethods();
	
	public class Util {
		public static EntityWithMethods fromTypeDeclaration(final TypeDeclaration type) {
			return new EntityWithMethods(){
				@Override public MethodDeclaration[] getMethods() {return type.getMethods();}
			};
		}
		
		public static EntityWithMethods fromAnonymousClass(final AnonymousClassDeclaration anon) {
			return new EntityWithMethods(){
				@Override public MethodDeclaration[] getMethods() {
					List<MethodDeclaration> mds = new ArrayList<MethodDeclaration>(anon.bodyDeclarations().size());
					for( Object md_ : anon.bodyDeclarations() ) {
						if( md_ instanceof MethodDeclaration )
							mds.add((MethodDeclaration)md_);
					}
					return mds.toArray(new MethodDeclaration[mds.size()]);
				}
			};
		}
	}
}
