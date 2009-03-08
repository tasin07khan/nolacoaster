package edu.cmu.cs.nbeckman.arkan8id.gameobjects;

/**
 * This class controls the movement of the 'camera' that will perform the roll
 * of keeping the ball onscreen.
 * 
 * Surprisingly, it does a few non-trivial things. Although it's really more important
 * that other objects in the game know how to draw themselves when the camera moves.
 * 
 * @author Nels E. Beckman
 */
public class Camera {

	private static final int CHASE_SPEED = 10;
	
	private final int screenHeight;
	private final int oneEighthOfScreenHeight;
	
	private int velocity;
	
	private int logicalYOfScreenBottom;
	
	private boolean chasing;
	private int chasePointY;
	
	/**
	 * Creates a new camera, with the assumption that 0 should be the
	 * bottom of the initial shot.
	 */
	public Camera(int screenHeight) {
		this.screenHeight = screenHeight;
		this.oneEighthOfScreenHeight = this.screenHeight / 8;
		this.velocity = 0;
		this.logicalYOfScreenBottom = 0;
		this.chasing = false;
		this.chasePointY = 0;
	}
	
	/**
	 * Tell the camera that one step of the game has passed. This will allow the
	 * camera to recalculate its position. This is necessary since the camera
	 * actually has a velocity and moves. It must be passed the current ball
	 * position so that it can track the ball.
	 * 
	 * @param ballPosition
	 */
	public void step(HasBoundingBox ballPosition) {
		if( chasing ) {
			// Adjust position
			this.logicalYOfScreenBottom += velocity;
			// See if you have made it to the chase point or overshot. 
			if( (this.velocity > 0 && this.logicalYOfScreenBottom >= this.chasePointY) || 
			    (this.velocity < 0 && this.logicalYOfScreenBottom <= this.chasePointY) ) {
				this.logicalYOfScreenBottom = this.chasePointY;
				this.velocity = 0;
				this.chasing = false;
			}
		}
		else {
			// See if we need to chase. There are a couple of case.
			if( ballPosition.getY() < this.screenHeight &&
				this.logicalYOfScreenBottom != 0 ) {
				// Check to see if the ball is entering the bottom screen.
				// if so, chase to zero.
				this.velocity = -CHASE_SPEED;
				this.chasePointY = 0;
				this.chasing = true;
			}
			else if( ballPosition.getY() >= this.screenHeight && 
					 this.logicalYOfScreenBottom == 0) {
				// This should be the case when the ball first leaves the home screen.
				chase(ballPosition, true);
			}
			else if( ballPosition.getY() >= 
				     (this.logicalYOfScreenBottom + this.screenHeight - this.oneEighthOfScreenHeight) ) {
				// normal chase, see if ball has gone above the 1/8 from top mark
				chase(ballPosition, true);
			}
			else if( ballPosition.getY() < (this.logicalYOfScreenBottom + this.oneEighthOfScreenHeight) &&
					 this.logicalYOfScreenBottom != 0 ) {
				// normal chase, see if ball has gone below the 1/8 from the bottom
				chase(ballPosition, false);
			}
		}
	}

	private void chase(HasBoundingBox ballPosition, boolean chaseUp) {
		this.velocity = chaseUp ? CHASE_SPEED : -CHASE_SPEED;
		// Put the ball in the middle of the screen
		this.chasePointY = ballPosition.getY() - (this.screenHeight / 2);
		this.chasing = true;
	}
	
	/**
	 * What is the logical Y value of the lowest on-screen point? This is essentially
	 * the calculated result of the camera, and will allow other objects to draw themselves
	 * appropriately.
	 */
	public int getLogicalYOfScreenBottom() {
		return this.logicalYOfScreenBottom;
	}
	
}
