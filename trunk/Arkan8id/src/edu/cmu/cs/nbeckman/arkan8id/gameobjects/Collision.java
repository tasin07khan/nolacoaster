package edu.cmu.cs.nbeckman.arkan8id.gameobjects;

/**
 * A 'enum' class for determining the type of collision that occurred.
 * 
 * @author Nels E. Beckman
 */
public final class Collision {
	private Collision(){};
	
	public static final Collision NO_COLLISION = new Collision();
	public static final Collision VERTICAL_COLLISION = new Collision();
	public static final Collision HORIZONTAL_COLLISION = new Collision();
	public static final Collision ANGLE_COLLISION = new Collision();
	public static final Collision GLANCING_BLOW = new Collision();
}
