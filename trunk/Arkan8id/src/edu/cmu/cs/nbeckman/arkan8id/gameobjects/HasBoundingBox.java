package edu.cmu.cs.nbeckman.arkan8id.gameobjects;

/**
 * Says that this is some kind of shape that has a bounding box, a position and
 * a height and width.
 * 
 * @author Nels E. Beckman
 *
 */
public interface HasBoundingBox {

	public int getX();
	public int getY();
	
	public int getHeight();
	public int getWidth();
	
}
