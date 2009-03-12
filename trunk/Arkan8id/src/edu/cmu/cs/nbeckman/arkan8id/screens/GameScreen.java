package edu.cmu.cs.nbeckman.arkan8id.screens;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.container.FullScreen;
import edu.cmu.cs.nbeckman.arkan8id.gameobjects.Ball;
import edu.cmu.cs.nbeckman.arkan8id.gameobjects.BrickBoard;
import edu.cmu.cs.nbeckman.arkan8id.gameobjects.Camera;
import edu.cmu.cs.nbeckman.arkan8id.gameobjects.Collision;
import edu.cmu.cs.nbeckman.arkan8id.gameobjects.HasBoundingBox;
import edu.cmu.cs.nbeckman.arkan8id.gameobjects.Spaceship;
import edu.cmu.cs.nbeckman.arkan8id.gameobjects.Collision.CollisionType;
import edu.cmu.cs.nbeckman.arkan8id.graphics.GraphicOps;


/**
 * The actual game screen.
 * 
 * @author Nels E. Beckman
 *
 */
public class GameScreen extends FullScreen {

	// Affects the speed of movement of the craft. 
	// We multiple wheel movement by this amount.
	private static final int MOVEMENT_MULTIPLIER = 4;

	// There is a race on this field, but one that will be benign.
	private boolean gameNotYetEnded = true;
	
	private final int screenWidth;
	
	private final GraphicOps graphics;
	
	private final Spaceship spaceship;
	
	private final BrickBoard bricks;
	
	private final Ball ball;
	
	private final Camera camera;
	
	private long bricksDestroyed;
	
	/**
	 * Has the game not yet ended? True until the game is over, including during
	 * initialization.
	 * @return
	 */
	public boolean gameNotYetEnded() {
		return this.gameNotYetEnded;
	}
	
	protected void paint(Graphics graphics) {
		final int logicalYOfScreenBottom = this.camera.getLogicalYOfScreenBottom();
		this.graphics.draw(graphics, spaceship, ball, logicalYOfScreenBottom);
		this.bricks.drawBoard(graphics, logicalYOfScreenBottom);
		this.graphics.drawStats(graphics, ball, bricksDestroyed);
	}
	
	public GameScreen(int screenWidth, int screenHeight) {
		this.graphics = new GraphicOps(screenWidth, screenHeight);
		this.spaceship = new Spaceship(screenWidth, screenHeight);
		this.ball = new Ball(40,40,5,5);
		this.bricks = new BrickBoard(screenWidth, screenHeight, 5);
		this.screenWidth = screenWidth;
		this.camera = new Camera(screenHeight);
		this.bricksDestroyed = 0L;
		// Start the thread that will update graphics
		// and move the ball.
		(new LoopingThread()).start();
	}

	/**
	 * Handle button pressed.
	 */
	protected boolean keyChar(char c, int status, int time) {
		switch( c ) {
		case Characters.ESCAPE:
			this.gameNotYetEnded = false;
			return true;
		default:
			return false;
		}
	}

	/**
	 * Handle a movement of the trackball.
	 */
	protected boolean navigationMovement(int dx, int dy, int status, int time) {
		if( dx != 0 ) {
			// the wheel was moved left or right.
			if( dx < 0 )
				spaceship.moveLeft(Math.abs(dx) * MOVEMENT_MULTIPLIER);
			else
				spaceship.moveRight(Math.abs(dx) * MOVEMENT_MULTIPLIER);
		}
		
		return true;
	}	
	
	// Did the ball collide with the left wall?
	private boolean checkCollisionWithLeftWall(HasBoundingBox ball) {
		return ball.getX() < 0;
	}
	
	// Did the ball collide with the right wall?
	private boolean checkCollosionWithRightWall(HasBoundingBox ball) {
		return (ball.getX()+ball.getWidth()) >= screenWidth;
	}
	
	// Did the ball go off the bottom?
	private boolean isBallLost(HasBoundingBox ball) {
		return (ball.getY()+ball.getHeight()) < 0;
	}
	
	private class LoopingThread extends Thread {
		
		public void run() {
			while( GameScreen.this.gameNotYetEnded ) {
				// Move ball
				GameScreen.this.ball.step();
				// See if ball collided with a brick
				Collision collision = GameScreen.this.bricks.ballMovedCheckCollision(GameScreen.this.ball);
				GameScreen.this.ball.respondToCollision(collision.getType());
				GameScreen.this.camera.step(GameScreen.this.ball);
				GameScreen.this.bricksDestroyed += collision.getNumDestroyed();
				
				// Every 100 points, try to garbage collect the lower screenfulls...
				if( (collision.getNumDestroyed() > 0) && (GameScreen.this.bricksDestroyed % 104 == 0) ) {
					GameScreen.this.bricks.garbageCollect();
				}
				
				// See if ball collided with a wall
				if( checkCollisionWithLeftWall(GameScreen.this.ball) ) {
					GameScreen.this.ball.respondToCollision(Collision.CollisionType.VERTICAL_COLLISION);
				}
				else if( checkCollosionWithRightWall(GameScreen.this.ball)) {
					GameScreen.this.ball.respondToCollision(Collision.CollisionType.VERTICAL_COLLISION);
				}
				
				// See if ball collided with the paddle
				CollisionType paddle_collision = GameScreen.this.spaceship.collidesWithShip(ball);
				GameScreen.this.ball.respondToCollision(paddle_collision);
				
				// If the ball goes off the bottom, just start the whole thing over
				// again. This will work just fine for now.
				if( isBallLost(ball) ) {
					ball.reset();
				}
				
				GameScreen.this.invalidate();
				
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// Doesn't matter because we are just looping forever.
				}
				
			}
		}
		
	}
}
