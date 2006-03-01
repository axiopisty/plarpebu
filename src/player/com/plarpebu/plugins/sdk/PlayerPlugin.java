package com.plarpebu.plugins.sdk;

import java.util.Map;

import com.plarpebu.javazoom.jlgui.basicplayer.BasicController;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicPlayerEvent;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicPlayerListener;

import fr.unice.plugin.Plugin;

public interface PlayerPlugin extends Plugin {

    /**
     * Open callback, stream is ready to play. properties map includes audio
     * format dependant features such as bitrate, duration, frequency, channels,
     * number of frames, vbr flag, ...
     * 
     * @param stream
     *        could be File, URL or InputStream
     * @param properties
     *        audio stream properties.
     */
    public void opened(Object stream, Map properties);

    /**
     * Progress callback while playing. This method is called severals time per
     * seconds while playing. properties map includes audio format features such
     * as instant bitrate, microseconds position, current frame number, ...
     * 
     * @param bytesread
     *        from encoded stream.
     * @param microseconds
     *        elapsed (<b>reseted after a seek !</b>).
     * @param pcmdata
     *        PCM samples.
     * @param properties
     *        audio stream parameters.
     */
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties);

    /**
     * Notification callback of javazoom.jlgui.player.test state.
     * 
     * @param event
     */
    public void stateUpdated(BasicPlayerEvent event);

    /**
     * A handle to the BasicPlayer, plugins may control the player through the
     * controller (play, stop, etc...)
     * 
     * @param controller :
     *        a handle to the player
     */
    public void setController(BasicController controller);

    public BasicPlayerListener getPlugin();

}
