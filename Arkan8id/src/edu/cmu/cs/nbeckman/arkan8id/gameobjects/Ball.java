package edu.cmu.cs.nbeckman.arkan8id.gameobjects;

import edu.cmu.cs.nbeckman.arkan8id.gameobjects.Collision.CollisionType;
import net.rim.device.api.system.Bitmap;

public class Ball implements HasBoundingBox {

	private static final Bitmap BALL_IMAGE = Bitmap.getBitmapResource("BlueBall.png");
	
	/*
	 * Store initial values...
	 */
	private final int initialPoxX;
	private final int initialPoxY;
	private final int initialVelX;
	private final int initialVelY;
	
	/*
	 * Current position
	 */
	private int posX;
	private int posY;
	
	/*
	 * Current velocity
	 */
	private int velX;
	private int velY;
	
	private long ballsLost;
	
	public Ball(int posX, int posY, int velX, int velY) {
		super();
		this.posX = posX;
		this.posY = posY;
		this.velX = velX;
		this.velY = velY;
		
		this.initialPoxX = posX;
		this.initialPoxY = posY;
		this.initialVelX = velX;
		this.initialVelY = velY;
		
		this.ballsLost = 0;
	}

	public int getHeight() {
		return BALL_IMAGE.getHeight();
	}

	public int getWidth() {
		return BALL_IMAGE.getWidth();
	}

	public int getX() {
		return posX;
	}

	public int getY() {
		return posY;
	}
	
	/**
	 * Move one step...
	 */
	public void step() {
		this.posX += velX;
		this.posY += velY;
	}

	/**
	 * Reset ball to initial position and velocity.
	 */
	public void reset() {
		this.posX = initialPoxX;
		this.posY = initialPoxY;
		this.velX = initialVelX;
		this.velY = initialVelY;
		
		this.ballsLost ++;
	}
	
	public int getVelX() {
		return velX;
	}

	public void setVelX(int velX) {
		this.velX = velX;
	}

	public int getVelY() {
		return velY;
	}

	public void setVelY(int velY) {
		this.velY = velY;
	}

	public Bitmap getImage() {
		return BALL_IMAGE;
	}

	public void respondToCollision(CollisionType collision) {
		if( collision == Collision.CollisionType.ANGLE_COLLISION ) {
			setVelX(-getVelX());
			setVelY(-getVelY());
		}
		else if( collision == Collision.CollisionType.HORIZONTAL_COLLISION ) {
			setVelY(-getVelY());
		}
		else if( collision == Collision.CollisionType.VERTICAL_COLLISION ) {
			setVelX(-getVelX());
		} else if( collision == Collision.CollisionType.GLANCING_BLOW || 
				   collision == Collision.CollisionType.NO_COLLISION) {
			// keep same velocity
		}
	}

	public long ballsLost() {
		return this.ballsLost;
	}
}
