package edu.cmu.cs.nbeckman.arkan8id.gameobjects;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;

/**
 * The BrickBoard, a linked list of 'byte' arrays that represents the infinite rows of 
 * bricks in the game. This should have a very small public interface, only the ability
 * to draw the board and to detect collisions with the board. This is probably the most
 * complex class in the program, but hopefully that complexity will not be exposed outside
 * of the class.
 * 
 * @author Nels E. Beckman
 */
public final class BrickBoard {

	private static final int WACKY_SCREEN_HEIGHT_DIVISOR = 13;
	private static final int WACKY_SCREEN_WIDTH_DIVISOR = 8;
	private static final boolean DEBUG = false;
	
	private static final Bitmap BRICK_IMAGE = Bitmap.getBitmapResource("RedBrick.png");
	
	private final int screenWidth;
	private final int screenHeight;
	
	private final int brickHeight;
	private final int brickWidth;
	
	private Screenful currentScreenFull;
	
	
	public BrickBoard(int screenWidth, int screenHeight, int bricksInFirstScreen) {
		super();
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.brickHeight = screenHeight / WACKY_SCREEN_HEIGHT_DIVISOR;
		this.brickWidth = screenWidth / WACKY_SCREEN_WIDTH_DIVISOR;
		this.currentScreenFull = new Screenful(bricksInFirstScreen, this.brickHeight);
	}
	
	/**
	 * This class holds the byte array, and is responsible for converting
	 * bytes into logical locations. It is also essentially a linked list of
	 * arrays.
	 */
	private final class Screenful {
		private final int logicalYOfBottom;
		private final char[] brickHolder;
				
		private Screenful next;
		final private Screenful prev;
		
		// Call for the initial screen full
		Screenful(int numBricksInIntial, int brickHeight) {
			this(BOTTOM_SCREENFUL, END_SCREENFUL, 
					screenHeight - (numBricksInIntial * brickHeight), numBricksInIntial);
		}
		
		// Call for subsequent screenfulls.
		Screenful(Screenful prev, Screenful next, int logicalYOfBottom) {
			this(prev, next, logicalYOfBottom, WACKY_SCREEN_HEIGHT_DIVISOR);
		}
		
		Screenful(Screenful prev, Screenful next, int logicalYOfBottom, int arraySize) {
			this.prev = prev;
			this.next = next;
			this.logicalYOfBottom = logicalYOfBottom;
			
			this.brickHolder = new char[arraySize];
			for( int i=0;i<this.brickHolder.length;i++) {
				this.brickHolder[i] = '\u00FF';
			}
		}
		
		int getLogicalYOfBottom() {
			return logicalYOfBottom;
		}
		
		int getLogicalYOfTop() {
			return logicalYOfBottom + (brickHolder.length * brickHeight);
		}
		
		Screenful getPrev() {
			return prev;
		}

		Screenful getNext() {
			return next;
		}
		
		void setNext(Screenful next) {
			this.next = next;
		}

		// Draw this entire screenful (even though it could be off screen)
		// w.r.t. the logicalY, which basically means subtracting logicalY
		// from every point that you would draw.
		void draw(Graphics graphics, int logicalY) {
			for(int i = 0; i < brickHolder.length; i++) {
				char row = brickHolder[i];
				int brick_bottom_y = (this.getLogicalYOfBottom() + i * brickHeight) - logicalY;
				int flipped_bottom_y = screenHeight - brick_bottom_y - brickHeight;
				drawChar(graphics, row, flipped_bottom_y);
			}
		}

		private void drawChar(Graphics graphics, char row, int brick_bottom_y) {
			
			for( char i=0;i<8;i++ ) {
				// is this bit on?
				char MASK = (char) (1 << i);
				if( (row&MASK) == MASK  ) {
					// Let's try a bitmap
					int x = i * brickWidth;
					int y = brick_bottom_y;
					graphics.drawBitmap(x, y, brickWidth, brickHeight, BRICK_IMAGE, 0, 0);
				}
			}
		}

		// XXX So UGLY!
		Collision collide(HasBoundingBox ball) {
			// Go in order through the 4 points of the ball's bounding box.
			int bot_l_x = ball.getX(); 
			int bot_l_y = ball.getY();
			boolean bot_l_collided = false;
			
			int bot_r_x = ball.getX() + ball.getWidth(); 
			int bot_r_y = bot_l_y;
			boolean bot_r_collided = false;
			
			int top_l_x = bot_l_x;
			int top_l_y = ball.getY() + ball.getHeight();
			boolean top_l_collided = false;
			
			int top_r_x = bot_r_x;
			int top_r_y = top_l_y;
			boolean top_r_collided = false;
			
			if( pointIsOnScreenful(bot_l_x, bot_l_y) ) {
				bot_l_collided = doesCollide(bot_l_x, bot_l_y);
			}
			if( pointIsOnScreenful(bot_r_x, bot_r_y) ) {
				bot_r_collided = doesCollide(bot_r_x, bot_r_y);
			}
			if( pointIsOnScreenful(top_l_x, top_l_y) ) {
				top_l_collided = doesCollide(top_l_x, top_l_y);
			}
			if( pointIsOnScreenful(top_r_x, top_r_y) ) {
				top_r_collided = doesCollide(top_r_x, top_r_y);
			}
			
			if( bot_l_collided && bot_r_collided && top_l_collided && top_r_collided ) {
				// Hope this never happens because it doesn't make too much sense.
				// Just return something...
				turnOffIfOn(bot_l_x, bot_l_y);
				turnOffIfOn(bot_r_x, bot_r_y);
				turnOffIfOn(top_l_x, top_l_y);
				turnOffIfOn(top_r_x, top_r_y);
				return Collision.HORIZONTAL_COLLISION;
			}
			else if(bot_l_collided&&top_l_collided&&top_r_collided) {
				// Every group of three...
				turnOffIfOn(bot_l_x, bot_l_y);
				turnOffIfOn(top_l_x, top_l_y);
				turnOffIfOn(top_r_x, top_r_y);
				return Collision.ANGLE_COLLISION;
			}
			else if(top_l_collided&&top_r_collided&&bot_r_collided) {
				// Every group of three...
				turnOffIfOn(top_l_x, top_l_y);
				turnOffIfOn(top_r_x, top_r_y);
				turnOffIfOn(bot_r_x, bot_r_y);
				return Collision.ANGLE_COLLISION;
			}
			else if(top_r_collided&&bot_r_collided&&bot_l_collided) {
				// Every group of three...
				turnOffIfOn(top_r_x, top_r_y);
				turnOffIfOn(bot_r_x, bot_r_y);
				turnOffIfOn(bot_l_x, bot_l_y);
				return Collision.ANGLE_COLLISION;				
			}
			else if(bot_r_collided&&bot_l_collided&&top_l_collided) {
				// Every group of three...
				turnOffIfOn(bot_r_x, bot_r_y);
				turnOffIfOn(bot_l_x, bot_l_y);
				turnOffIfOn(top_l_x, top_l_y);
				return Collision.ANGLE_COLLISION;				
			}
			else if(bot_r_collided && top_r_collided) {
				// Either both rs or both ls
				turnOffIfOn(bot_r_x, bot_r_y);
				turnOffIfOn(top_r_x, top_r_y);
				return Collision.VERTICAL_COLLISION;
			}
			else if(top_l_collided && bot_l_collided) {
				// Either both rs or both ls
				turnOffIfOn(top_l_x, top_l_y);
				turnOffIfOn(bot_l_x, bot_l_y);
				return Collision.VERTICAL_COLLISION;				
			}
			else if(top_l_collided && top_r_collided) {
				// Either both tops of both bottoms
				turnOffIfOn(top_l_x, top_l_y);
				turnOffIfOn(top_r_x, top_r_y);
				return Collision.HORIZONTAL_COLLISION;
			}
			else if(bot_r_collided && bot_l_collided) {
				// Either both tops of both bottoms
				turnOffIfOn(bot_r_x, bot_r_y);
				turnOffIfOn(bot_l_x, bot_l_y);
				return Collision.HORIZONTAL_COLLISION;				
			}
			else if(bot_l_collided) {
				// Single point
				turnOffIfOn(bot_l_x, bot_l_y);
				return Collision.GLANCING_BLOW;	
			}
			else if(top_l_collided) {
				// Single point
				turnOffIfOn(top_l_x, top_l_y);
				return Collision.GLANCING_BLOW;	
			}
			else if(top_r_collided) {
				// Single point
				turnOffIfOn(top_r_x, top_r_y);
				return Collision.GLANCING_BLOW;	
			}
			else if(bot_r_collided) {
				// Single point
				turnOffIfOn(bot_r_x, bot_r_y);
				return Collision.GLANCING_BLOW;	
			}
			else {
				return Collision.NO_COLLISION;
			}
			
		}
		
		
		private int getArrayIndexFromY(int y) {
			return (y - this.getLogicalYOfBottom()) / brickHeight;
		}
		
		private int getBitShiftFromX(int x) {
			return x / brickWidth;
		}
		
		/** This method just checks to see if the point collides. 
		 * PRE: Point must be in this screenful.
		 * */
		private boolean doesCollide(int x, int y) {
			char row = brickHolder[getArrayIndexFromY(y)];
			
			int MASK = 1 << getBitShiftFromX(x);
			return (row&MASK) == MASK;
		}
		
		/** If this brick is on, turn it off.
		 * PRE: Point must be in this screenful.
		 *  */
		private void turnOffIfOn(int x, int y) {
			final int array_index = getArrayIndexFromY(y);
			char row = brickHolder[array_index];
			
			int MASK = 1 << getBitShiftFromX(x);
			brickHolder[array_index] = (char) (row & (~MASK));
		}

		// Is the given point even in this screenfull?
		private boolean pointIsOnScreenful(int x, int y) {
			return (x >= 0) && (x < screenWidth) &&
			       (y >= this.getLogicalYOfBottom()) &&
			       (y < this.getLogicalYOfTop());
		}
	}
	
	/** The terminator for the very top of the bricks */
	private static final Screenful END_SCREENFUL = (new BrickBoard(0,0,0)).new Screenful(null,null,0,0);
	/** The terminator for the very bottom of the bricks, which is visible from the start */
	private static final Screenful BOTTOM_SCREENFUL = (new BrickBoard(0,0,0)).new Screenful(null,null,0,0);
	
	/**
	 * Calling this method reports to the BrickBoard that the ball has moved. The
	 * BrickBoard needs to be told so that it can extend the list of screen-fulls.
	 * Additionally, if the movement results in a collision, this method returns
	 * a Collision object indicating what happened.
	 * 
	 * @param ball The location and shape of the ball in its new position.
	 * @return a Collision object indicating what happened.
	 */
	public Collision ballMovedCheckCollision(HasBoundingBox ball) {
		this.currentScreenFull = 
			findLowestScreenfulOnScreen(ball.getY(), currentScreenFull);
			//findScreenHoldingBall(ball, currentScreenFull);
		
		// We check the screenfull returned AND the next for collisions
		// but bottom has priority...
		
		Collision bottomCollision = this.currentScreenFull.collide(ball);
		Collision topCollision = this.currentScreenFull.getNext().collide(ball);
		
		if( bottomCollision == Collision.NO_COLLISION )
			return topCollision;
		else
			return bottomCollision;
	}

	/**
	 * Draw this gameboard starting at the given logical y location. This logical
	 * y will be the bottom of the screen that we will draw. So, e.g., at the beginning
	 * before the ball starts moving, the logical y should be 0.
	 * @param logicalY
	 */
	public void drawBoard(Graphics graphics, int logicalY) {
		currentScreenFull = 
			findLowestScreenfulOnScreen(logicalY, this.currentScreenFull);
		currentScreenFull.draw(graphics, logicalY);
		currentScreenFull.getNext().draw(graphics, logicalY);
	}

	// 1.) find the first screenful such that logicalY is in between its top & bottom OR
	//     its bottom is equal to logicalY.
	private Screenful findLowestScreenfulOnScreen(int logicalY, Screenful currentScreenful) {
		while( logicalY > currentScreenful.getLogicalYOfTop() ||
			   logicalY < currentScreenful.getLogicalYOfBottom() ) {
			if( logicalY > currentScreenful.getLogicalYOfTop() ) {
				// Go up the chain...
				Screenful next = currentScreenful.getNext();
				if( next == END_SCREENFUL ) {
					System.err.println("Creating a new Screenful");
					Screenful newScreenful = new Screenful(currentScreenful, END_SCREENFUL, currentScreenful.getLogicalYOfTop());
					currentScreenful.setNext(newScreenful);
					currentScreenful = newScreenful;
				}
				else {
					currentScreenful = next;
				}
			} 
			else {
				// Go down the chain...
				Screenful prev = currentScreenful.getPrev();
				if( prev == BOTTOM_SCREENFUL ) {
					// The logical Y can be below the bottom-most screenful, in which case we should
					// just draw the top one.
					return currentScreenful;
				}
				else {
					currentScreenful = currentScreenful.getPrev();
				}
			}
		}
		
		// Right before we return, if the next screenful happens to be empty, create a new
		// one. This will ensure that even if the first one is all the way to the bottom, there
		// will always be something to see on the next screen full.
		if( currentScreenful.getNext() == END_SCREENFUL ) {
			currentScreenful.setNext(new Screenful(currentScreenful, END_SCREENFUL, currentScreenful.getLogicalYOfTop()));
		}
		
		return currentScreenful;
	}	
}