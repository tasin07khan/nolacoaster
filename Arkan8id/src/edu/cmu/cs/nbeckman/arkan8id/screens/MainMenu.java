package edu.cmu.cs.nbeckman.arkan8id.screens;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public final class MainMenu extends MainScreen {

	private GameScreen gameScreen;
	private int terminationLookInvokeID;
	
	private ButtonField startButton = 
		new ButtonField("Start!", ButtonField.HCENTER | ButtonField.FIELD_BOTTOM) {

		protected boolean trackwheelClick(int status, int time) {
			// Here you want to actually run the game.
			
			// Need to use Display.getWidth() but that
			// requires having a "signed" app and I'm not
			// sure if I'll be able to do that yet.
			MainMenu.this.gameScreen = 
				new GameScreen(Graphics.getScreenWidth(), Graphics.getScreenHeight());
			
			MainMenu.this.getUiEngine().pushScreen(MainMenu.this.gameScreen);
			
			// A loop to wait until the application has ended.
			MainMenu.this.terminationLookInvokeID =
			MainMenu.this.getApplication().invokeLater(new Runnable() {
				public void run() {					
					if( !MainMenu.this.gameScreen.gameNotYetEnded() ) {
						// Stop running this very loop we are in
						getApplication().cancelInvokeLater(MainMenu.this.terminationLookInvokeID);

						// Pop the game screen which ends everything.
						getUiEngine().popScreen(MainMenu.this.gameScreen);
					}
				}}, 500, true);
			
			// Event consumed
			return true;
		}
		
	};
	
	public MainMenu() {
		// Do most of the layout initialization here.
		super(NO_VERTICAL_SCROLL);
		LabelField title = new LabelField("Arkan\u221eid", LabelField.FIELD_HCENTER);
        this.setTitle(title);
                
        this.add(startButton);
	}
	
}
