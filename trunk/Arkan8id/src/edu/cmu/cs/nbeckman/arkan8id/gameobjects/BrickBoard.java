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
	
	private static final Bitmap BRICK_IMAGE = Bitmap.getBitmapResource("RedBrick.png");
	
	private final int screenWidth;
	private final int screenHeight;
	
	private final int brickHeight;
	private final int brickWidth;
	
	private IScreenful currentScreenFull;
	
	
	public BrickBoard(int screenWidth, int screenHeight, int bricksInFirstScreen) {
		super();
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.brickHeight = screenHeight / WACKY_SCREEN_HEIGHT_DIVISOR;
		this.brickWidth = screenWidth / WACKY_SCREEN_WIDTH_DIVISOR;
		this.currentScreenFull = new Screenful(bricksInFirstScreen, this.brickHeight);
	}
	
	private interface IScreenful {
		long getLogicalYOfBottom();
		long getLogicalYOfTop();
		IScreenful getPrev();
		IScreenful getNext();
		void setNext(IScreenful next);
		void draw(Graphics graphics, long logicalY);
		Collision collide(HasBoundingBox ball);
		boolean isTerminator();
		/**
		 * Is every single row empty?
		 */
		boolean isCompletelyEmpty();
		/**
		 * Does this screenful contain an array of bricks?
		 */
		boolean containsBrickArray();
		void setPrev(IScreenful new_sf);
	}
	
	private final static class NullScreenful implements IScreenful {

		public Collision collide(HasBoundingBox ball) {
			return Collision.emptyCollision();
		}

		public void draw(Graphics graphics, long logicalY) {}

		public long getLogicalYOfBottom() {
			throw new UnsupportedOperationException();
		}

		public long getLogicalYOfTop() {
			throw new UnsupportedOperationException();
		}

		public IScreenful getNext() {
			throw new UnsupportedOperationException();
		}

		public IScreenful getPrev() {
			throw new UnsupportedOperationException();
		}

		public void setNext(IScreenful next) {
			throw new UnsupportedOperationException();
		}

		public boolean isCompletelyEmpty() {
			throw new UnsupportedOperationException();
		}

		public boolean isTerminator() {
			return true;
		}

		public boolean containsBrickArray() {
			return false;
		}

		public void setPrev(IScreenful new_sf) {
			throw new UnsupportedOperationException();
		}
	}
	
	private final static class EmptyScreenful implements IScreenful {

		private final long top;
		private final long bottom;
		private IScreenful next;
		private IScreenful prev;

		public EmptyScreenful(long bottom, long top, IScreenful prev,
				IScreenful next) {
			super();
			this.bottom = bottom;
			this.top = top;
			this.prev = prev;
			this.next = next;
		}

		public Collision collide(HasBoundingBox ball) {
			return Collision.emptyCollision();
		}

		public void draw(Graphics graphics, long logicalY) {}

		public long getLogicalYOfBottom() {
			return bottom;
		}

		public long getLogicalYOfTop() {
			return top;
		}

		public IScreenful getNext() {
			return next;
		}

		public IScreenful getPrev() {
			return prev;
		}

		public void setNext(IScreenful next) {
			this.next = next;
		}

		public boolean isCompletelyEmpty() {
			return true;
		}

		public boolean isTerminator() {
			return false;
		}

		public boolean containsBrickArray() {
			return false;
		}

		public void setPrev(IScreenful prev) {
			this.prev = prev;
		}
	}
	
	/**
	 * This class holds the byte array, and is responsible for converting
	 * bytes into logical locations. It is also essentially a linked list of
	 * arrays.
	 */
	private final class Screenful implements IScreenful {
		private final long logicalYOfBottom;
		private final byte[] brickHolder;
				
		private IScreenful next;
		private IScreenful prev;
		
		// Call for the initial screen full
		Screenful(int numBricksInIntial, int brickHeight) {
			this(BOTTOM_SCREENFUL, END_SCREENFUL, 
					screenHeight - (numBricksInIntial * brickHeight), numBricksInIntial);
		}
		
		// Call for subsequent screenfulls.
		Screenful(IScreenful prev, IScreenful next, long logicalYOfBottom) {
			this(prev, next, logicalYOfBottom, WACKY_SCREEN_HEIGHT_DIVISOR);
		}
		
		Screenful(IScreenful prev, IScreenful next, long logicalYOfBottom, int arraySize) {
			this.prev = prev;
			this.next = next;
			this.logicalYOfBottom = logicalYOfBottom;
			
			this.brickHolder = new byte[arraySize];
			for( int i=0;i<this.brickHolder.length;i++) {
				this.brickHolder[i] = (byte) 0xFF;
			}
		}
		
		public long getLogicalYOfBottom() {
			return logicalYOfBottom;
		}
		
		public long getLogicalYOfTop() {
			return logicalYOfBottom + (brickHolder.length * brickHeight);
		}
		
		public IScreenful getPrev() {
			return prev;
		}

		public IScreenful getNext() {
			return next;
		}
		
		public void setNext(IScreenful next) {
			this.next = next;
		}

		// Draw this entire screenful (even though it could be off screen)
		// w.r.t. the logicalY, which basically means subtracting logicalY
		// from every point that you would draw.
		public void draw(Graphics graphics, long logicalY) {
			for(int i = 0; i < brickHolder.length; i++) {
				byte row = brickHolder[i];
				int brick_bottom_y = (int) ((int) (this.getLogicalYOfBottom() + ((long)i) * ((long)brickHeight)) - logicalY);
				int flipped_bottom_y = screenHeight - brick_bottom_y - brickHeight;
				drawbyte(graphics, row, flipped_bottom_y);
			}
		}

		private void drawbyte(Graphics graphics, byte row, int brick_bottom_y) {
			
			for( byte i=0;i<8;i++ ) {
				// is this bit on?
				byte MASK = (byte) (1 << i);
				if( (row&MASK) == MASK  ) {
					// Let's try a bitmap
					int x = i * brickWidth;
					int y = brick_bottom_y;
					graphics.drawBitmap(x, y, brickWidth, brickHeight, BRICK_IMAGE, 0, 0);
				}
			}
		}

		// XXX So UGLY!
		public Collision collide(HasBoundingBox ball) {
			// Go in order through the 4 points of the ball's bounding box.
			int bot_l_x = ball.getX(); 
			long bot_l_y = ball.getY();
			boolean bot_l_collided = false;
			
			int bot_r_x = ball.getX() + ball.getWidth(); 
			long bot_r_y = bot_l_y;
			boolean bot_r_collided = false;
			
			int top_l_x = bot_l_x;
			long top_l_y = ball.getY() + ball.getHeight();
			boolean top_l_collided = false;
			
			int top_r_x = bot_r_x;
			long top_r_y = top_l_y;
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
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(bot_l_x, bot_l_y);
				bricks_destroyed += turnOffIfOn(bot_r_x, bot_r_y);
				bricks_destroyed += turnOffIfOn(top_l_x, top_l_y);
				bricks_destroyed += turnOffIfOn(top_r_x, top_r_y);
				return new Collision(Collision.CollisionType.HORIZONTAL_COLLISION, bricks_destroyed);
			}
			else if(bot_l_collided&&top_l_collided&&top_r_collided) {
				// Every group of three...
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(bot_l_x, bot_l_y);
				bricks_destroyed += turnOffIfOn(top_l_x, top_l_y);
				bricks_destroyed += turnOffIfOn(top_r_x, top_r_y);
				return new Collision(Collision.CollisionType.ANGLE_COLLISION, bricks_destroyed);
			}
			else if(top_l_collided&&top_r_collided&&bot_r_collided) {
				// Every group of three...
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(top_l_x, top_l_y);
				bricks_destroyed += turnOffIfOn(top_r_x, top_r_y);
				bricks_destroyed += turnOffIfOn(bot_r_x, bot_r_y);
				return new Collision(Collision.CollisionType.ANGLE_COLLISION, bricks_destroyed);
			}
			else if(top_r_collided&&bot_r_collided&&bot_l_collided) {
				// Every group of three...
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(top_r_x, top_r_y);
				bricks_destroyed += turnOffIfOn(bot_r_x, bot_r_y);
				bricks_destroyed += turnOffIfOn(bot_l_x, bot_l_y);
				return new Collision(Collision.CollisionType.ANGLE_COLLISION, bricks_destroyed);				
			}
			else if(bot_r_collided&&bot_l_collided&&top_l_collided) {
				// Every group of three...
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(bot_r_x, bot_r_y);
				bricks_destroyed += turnOffIfOn(bot_l_x, bot_l_y);
				bricks_destroyed += turnOffIfOn(top_l_x, top_l_y);
				return new Collision(Collision.CollisionType.ANGLE_COLLISION, bricks_destroyed);				
			}
			else if(bot_r_collided && top_r_collided) {
				// Either both rs or both ls
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(bot_r_x, bot_r_y);
				bricks_destroyed += turnOffIfOn(top_r_x, top_r_y);
				return new Collision(Collision.CollisionType.VERTICAL_COLLISION, bricks_destroyed);
			}
			else if(top_l_collided && bot_l_collided) {
				// Either both rs or both ls
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(top_l_x, top_l_y);
				bricks_destroyed += turnOffIfOn(bot_l_x, bot_l_y);
				return new Collision(Collision.CollisionType.VERTICAL_COLLISION, bricks_destroyed);				
			}
			else if(top_l_collided && top_r_collided) {
				// Either both tops of both bottoms
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(top_l_x, top_l_y);
				bricks_destroyed += turnOffIfOn(top_r_x, top_r_y);
				return new Collision(Collision.CollisionType.HORIZONTAL_COLLISION, bricks_destroyed);
			}
			else if(bot_r_collided && bot_l_collided) {
				// Either both tops of both bottoms
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(bot_r_x, bot_r_y);
				bricks_destroyed += turnOffIfOn(bot_l_x, bot_l_y);
				return new Collision(Collision.CollisionType.HORIZONTAL_COLLISION, bricks_destroyed);				
			}
			else if(bot_l_collided) {
				// Single point
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(bot_l_x, bot_l_y);
				return new Collision(Collision.CollisionType.GLANCING_BLOW, bricks_destroyed);	
			}
			else if(top_l_collided) {
				// Single point
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(top_l_x, top_l_y);
				return new Collision(Collision.CollisionType.GLANCING_BLOW, bricks_destroyed);	
			}
			else if(top_r_collided) {
				// Single point
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(top_r_x, top_r_y);
				return new Collision(Collision.CollisionType.GLANCING_BLOW, bricks_destroyed);	
			}
			else if(bot_r_collided) {
				// Single point
				int bricks_destroyed = 0;
				bricks_destroyed += turnOffIfOn(bot_r_x, bot_r_y);
				return new Collision(Collision.CollisionType.GLANCING_BLOW, bricks_destroyed);	
			}
			else {
				return Collision.emptyCollision();
			}
			
		}
		
		
		private int getArrayIndexFromY(long y) {
			int rel_y = (int)(y - this.getLogicalYOfBottom()); 
			return  rel_y / brickHeight;
		}
		
		private int getBitShiftFromX(int x) {
			return x / brickWidth;
		}
		
		/** This method just checks to see if the point collides. 
		 * PRE: Point must be in this screenful.
		 * */
		private boolean doesCollide(int x, long y) {
			byte row = brickHolder[getArrayIndexFromY(y)];
			
			int MASK = 1 << getBitShiftFromX(x);
			return (row&MASK) == MASK;
		}
		
		/** If this brick is on, turn it off.
		 * PRE: Point must be in this screenful.
		 * @return returns the number of bricks that were actually turned off.
		 *  */
		private int turnOffIfOn(int x, long y) {
			final int result = doesCollide(x, y) ? 1 : 0;
			final int array_index = getArrayIndexFromY(y);
			byte row = brickHolder[array_index];
			
			int MASK = 1 << getBitShiftFromX(x);
			brickHolder[array_index] = (byte) (row & (~MASK));
			
			return result;
		}

		// Is the given point even in this screenfull?
		private boolean pointIsOnScreenful(int x, long y) {
			return (x >= 0) && (x < screenWidth) &&
			       (y >= this.getLogicalYOfBottom()) &&
			       (y < this.getLogicalYOfTop());
		}

		
		public boolean isCompletelyEmpty() {
			for( int i = 0; i<this.brickHolder.length; i++ ) {
				int row = this.brickHolder[i];
				if( (row != ((byte)0x0000)) ) {
					return false;
				}
			}
			return true;
		}

		public boolean isTerminator() {
			return false;
		}

		public boolean containsBrickArray() {
			return true;
		}

		public void setPrev(IScreenful prev) {
			this.prev = prev;
		}
	}
	
	/** The terminator for the very top of the bricks */
	private static final IScreenful END_SCREENFUL = new NullScreenful();
	/** The terminator for the very bottom of the bricks, which is visible from the start */
	private static final IScreenful BOTTOM_SCREENFUL = new NullScreenful();
	
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
		
		if( bottomCollision.getType() == Collision.CollisionType.NO_COLLISION )
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
	public void drawBoard(Graphics graphics, long logicalY) {
		currentScreenFull = 
			findLowestScreenfulOnScreen(logicalY, this.currentScreenFull);
		currentScreenFull.draw(graphics, logicalY);
		currentScreenFull.getNext().draw(graphics, logicalY);
	}

	// 1.) find the first screenful such that logicalY is in between its top & bottom OR
	//     its bottom is equal to logicalY.
	private IScreenful findLowestScreenfulOnScreen(long logicalY, IScreenful currentScreenful) {
		while( logicalY > currentScreenful.getLogicalYOfTop() ||
			   logicalY < currentScreenful.getLogicalYOfBottom() ) {
			if( logicalY > currentScreenful.getLogicalYOfTop() ) {
				// Go up the chain...
				IScreenful next = currentScreenful.getNext();
				if( next.isTerminator() ) {
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
				IScreenful prev = currentScreenful.getPrev();
				if( prev.isTerminator() ) {
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
		if( currentScreenful.getNext().isTerminator() ) {
			currentScreenful.setNext(new Screenful(currentScreenful, END_SCREENFUL, currentScreenful.getLogicalYOfTop()));
		}
		
		return currentScreenful;
	}
	
	/**
	 * This method will attempt to find any all-zero 
	 */
	public void garbageCollect() {
		/*
		 * Start with the very first screenful...
		 */
		IScreenful current = this.currentScreenFull;
		while( !current.getPrev().isTerminator() ) {
			current = current.getPrev();
		}
		
		/*
		 * Now go in the opposite direction.
		 * As long as this is not a terminator, if it contains a brick array and
		 * can the brick array is empty, we should replace it and move on.
		 */
		do {
			if( current.isTerminator() ) {
				return;
			}
			else if( current.containsBrickArray()) {
				if( current.isCompletelyEmpty() ) {
					// We can actually garbage collect!
					// Create a new one, setting the correct parameters
					IScreenful new_sf;
					
					// Figure our what to do about the previous screenful
					if( current.getPrev().isTerminator() ) {
						new_sf = new EmptyScreenful(current.getLogicalYOfBottom(),
								current.getLogicalYOfTop(), current.getPrev(), current.getNext());
					} else {
						// The previous one must be empty, right? So we
						// conjoin the two empty screenfulls...
						new_sf = new EmptyScreenful(current.getPrev().getLogicalYOfBottom(),
								current.getLogicalYOfTop(), current.getPrev().getPrev(), current.getNext());
					}
					
					// Then, adjust the prev of the next guy.
					if( !new_sf.getNext().isTerminator() ) {
						new_sf.getNext().setPrev(new_sf);
					}
					
					this.currentScreenFull = new_sf;
					current = new_sf.getNext();
				}
				else {
					return;
				}
			}
			else {
				current = current.getNext();
			}
		} while(true);
		
	}
}