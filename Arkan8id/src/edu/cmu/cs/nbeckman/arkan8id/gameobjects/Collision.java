package edu.cmu.cs.nbeckman.arkan8id.gameobjects;

/**
 * A 'enum' class for determining the type of collision that occurred.
 * 
 * @author Nels E. Beckman
 */
public final class Collision {
	
	public Collision(CollisionType type, int numDestroyed) {
		super();
		this.type = type;
		this.numDestroyed = numDestroyed;
	}

	private final CollisionType type;
	private final int numDestroyed;

	private static final Collision EMPTY_COLLISION = new Collision(CollisionType.NO_COLLISION, 0);
	
	public static final class CollisionType {
		private CollisionType() {};
		
		public static final CollisionType NO_COLLISION = new CollisionType();
		public static final CollisionType VERTICAL_COLLISION = new CollisionType();
		public static final CollisionType HORIZONTAL_COLLISION = new CollisionType();
		public static final CollisionType ANGLE_COLLISION = new CollisionType();
		public static final CollisionType GLANCING_BLOW = new CollisionType();
	}

	public CollisionType getType() {
		return this.type;
	}
	
	public static Collision emptyCollision() {
		return EMPTY_COLLISION;
	}

	public int getNumDestroyed() {
		return numDestroyed;
	}
}
