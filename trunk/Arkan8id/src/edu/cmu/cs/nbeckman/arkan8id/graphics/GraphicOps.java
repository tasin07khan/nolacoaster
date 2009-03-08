package edu.cmu.cs.nbeckman.arkan8id.graphics;

import edu.cmu.cs.nbeckman.arkan8id.gameobjects.Ball;
import edu.cmu.cs.nbeckman.arkan8id.gameobjects.Spaceship;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;

public final class GraphicOps {

	private final Bitmap backgroundImage;
	
	private final int screenWidth;
	private final int screenHeight;
	
	
	public GraphicOps(int screenWidth, int screenHeight) {
		this.screenHeight = screenHeight;
		this.screenWidth = screenWidth;
		
		this.backgroundImage = Bitmap.getBitmapResource("StarBackground.png");
		
		if( this.backgroundImage == null )
			throw new RuntimeException("Initialization failed.");
	}
	
	/**
	 * Draw one step of the game to the given graphics object.
	 * @param graphics
	 * @param ball 
	 * @param logicalYOfScreenBottom 
	 */
	public void draw(Graphics graphics, Spaceship spaceship, Ball ball, int logicalYOfScreenBottom) {
		graphics.drawBitmap(0, 0, 
				this.screenWidth, 
				this.screenHeight, 
				this.backgroundImage, 0, 0);
		
		graphics.drawBitmap(spaceship.getX(), 
				this.screenHeight - spaceship.getY() - spaceship.getImage().getHeight() + logicalYOfScreenBottom, 
				spaceship.getImage().getWidth(), 
				spaceship.getImage().getHeight(), 
				spaceship.getImage(), 
				0, 0);
		
		graphics.drawBitmap(ball.getX(), 
				this.screenHeight - ball.getY() - ball.getHeight() + logicalYOfScreenBottom, 
				ball.getImage().getWidth(), 
				ball.getImage().getWidth(), 
				ball.getImage(), 
				0, 
				0);
	}
	
}
