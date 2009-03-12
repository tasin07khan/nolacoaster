package edu.cmu.cs.nbeckman.arkan8id.gameobjects;

import net.rim.device.api.system.Bitmap;

/**
 * This object holds a ship!
 * 
 * @author Nels E. Beckman
 *
 */
public class Spaceship {
	
	private static final Bitmap image = Bitmap.getBitmapResource("Ship.png");
	
	private final int shipY;
	
	private final int maxX;  
	
	private int x;
	
	public Spaceship(int screenWidth, int screenHeight) {
		shipY = 30;
		maxX = screenWidth - image.getWidth();
		x = (screenWidth / 2) - (image.getWidth() / 2);
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return shipY;
	}
	
	/**
	 * Move the ship to the left x number of spaces. This method will reset the
	 * ship's location to 0 if it goes too far to the left.
	 * @param x
	 */
	public void moveLeft(int x) {
		this.x -= x;
		
		if( this.x < 0 ) 
			this.x = 0;
	}
	
	public void moveRight(int x) {
		this.x += x;
		
		if( this.x > maxX )
			this.x = maxX;
	}

	public Bitmap getImage() {
		return image;
	}
	
	private boolean isPointInShip(int x, long y) {
		if( getX() <= x && x <= (getX() + image.getWidth()) ) {
			if( getY() <= y && y <= (getY() + image.getHeight()) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Does the given object collide with the ship? 
	 * @return
	 */
	public Collision.CollisionType collidesWithShip(HasBoundingBox ball) {
		// if any of the four points is inside the ship...
		if( isPointInShip(ball.getX(), ball.getY()) ||
			isPointInShip(ball.getX(), ball.getY() + ball.getHeight()) ||
			isPointInShip(ball.getX() + ball.getWidth(), ball.getY()) ||
			isPointInShip(ball.getX() + ball.getWidth(), ball.getY() + ball.getHeight()) ) {
			return Collision.CollisionType.HORIZONTAL_COLLISION;
		}
		return Collision.CollisionType.NO_COLLISION;
	}
}