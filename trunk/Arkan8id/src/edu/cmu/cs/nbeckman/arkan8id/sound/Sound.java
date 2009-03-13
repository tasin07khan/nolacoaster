package edu.cmu.cs.nbeckman.arkan8id.sound;

import java.io.InputStream;

import javax.microedition.media.Player;

import net.rim.device.api.ui.component.Dialog;

public class Sound {
	private Player _musicPlayer; // Java media player
	  
	public static final String MUSIC_FILE = "/openhouseweekend.mid";
	
    /**
     * Start playing the midi file.
     */
    public void playMusic()
    {
        try
        {
            InputStream in = getClass().getResourceAsStream(MUSIC_FILE);
            _musicPlayer = javax.microedition.media.Manager.createPlayer(in, "audio/midi");
            _musicPlayer.realize();
            _musicPlayer.prefetch();
            _musicPlayer.setLoopCount(1);
            _musicPlayer.start();
 
        }
        catch (Exception e)
        {
            Dialog.alert("Error playing music");
        }
    }
 
    /**
     * Stop playing the midid file.
     */
    public void stopMusic()
    {
        try
        {
            _musicPlayer.stop();
        }
        catch (Exception e)
        {
            Dialog.alert("Error stopping music");
        }
        _musicPlayer.deallocate();
        _musicPlayer.close();
    }
}
