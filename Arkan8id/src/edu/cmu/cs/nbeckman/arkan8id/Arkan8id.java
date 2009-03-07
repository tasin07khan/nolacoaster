package edu.cmu.cs.nbeckman.arkan8id;

import edu.cmu.cs.nbeckman.arkan8id.screens.MainMenu;
import net.rim.device.api.ui.UiApplication;

/**
 * The main class of the Arkan8id game.
 * 
 * 
 * @author Nels E. Beckman
 *
 */
public class Arkan8id extends UiApplication {

	public static void main(String[] args) {
		(new Arkan8id()).runArkanoid();
	}

	private void runArkanoid() {
		this.pushScreen(new MainMenu());
		this.enterEventDispatcher();
	}
	
}
