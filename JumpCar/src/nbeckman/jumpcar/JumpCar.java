package nbeckman.jumpcar;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public final class JumpCar {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final List<Car> cars = Car.parseSpecFiles("cardata");
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create a JFrame, which is a Window with "decorations", i.e. 
                // title, border and close-button
                JFrame f = new JFrame("JumpCar");
 
                // Set a simple Layout Manager that arranges the contained 
                // Components
                f.setLayout(new FlowLayout());
 
                // Add some Components
                f.add(new JumpCarGUI(cars));
 
                // "Pack" the window, making it "just big enough".
                f.pack();
 
                // Set the default close operation for the window, or else the 
                // program won't exit when clicking close button
                //  (The default is HIDE_ON_CLOSE, which just makes the window
                //  invisible, and thus don't exit the app)
                f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
                // Set the visibility as true, thereby displaying it
                f.setVisible(true);
            }
        });

	}

}
