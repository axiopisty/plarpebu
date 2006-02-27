/**
 * BasicPlayer.
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package basicplayer;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import pluginsSDK.Iconifiable;

/**
 * BasicPlayer is a threaded audio javazoom.jlgui.player.test.
 */
/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class CompositePlayer extends BasicPlayer implements BasicController {
    private BasicPlayer mp3Player = new BasicMP3Player();

    private BasicPlayer midiKarPlayer = new BasicMidiPlayer();

    private BasicPlayer currentPlayer;

    private Iconifiable playerUI;

    public CompositePlayer() {
        setSupportedFileTypeExtensions();
    }

    public void open(InputStream in) throws BasicPlayerException {
        System.out.println("NOT IMPLEMENTED YET");
    }

    /**
     * Play a file
     */
    public void open(File file) throws BasicPlayerException {
        // If this is a zip file, we need to unzip it and play the mp3 file
        // inside.
        // We only support zipped mp3g files currently.
        if (file.getName().toLowerCase().endsWith(".zip")) file = ZipUtil.unzipMP3G(file);

        if (currentPlayer != null) currentPlayer.stop();

        String filename = file.getName().toLowerCase();
        if (filename.endsWith(".mid") || filename.endsWith(".kar")) {
            midiKarPlayer.open(file);
            currentPlayer = midiKarPlayer;
        }
        else {
            mp3Player.open(file);
            currentPlayer = mp3Player;
        }
    }

    public void open(URL url) throws BasicPlayerException {
        System.out.println("NOT IMPLEMENTED YET");
    }

    /**
     * Skip bytes.
     * 
     * @param bytes
     * @return bytes skipped according to audio frames constraint.
     * @throws BasicPlayerException
     */
    public long seek(long bytes) throws BasicPlayerException {
        if (currentPlayer != null) return currentPlayer.seek(bytes);

        return 0;
    }

    public void play() throws BasicPlayerException {
        if (currentPlayer != null) currentPlayer.play();
    }

    public void stop() throws BasicPlayerException {
        if (currentPlayer != null) currentPlayer.stop();
    }

    public void pause() throws BasicPlayerException {
        if (currentPlayer != null) currentPlayer.pause();
    }

    public void resume() throws BasicPlayerException {
        if (currentPlayer != null) currentPlayer.resume();
    }

    /**
     * Sets Pan (Balance) value. Linear scale : -1.0 <--> +1.0
     * 
     * @param pan
     *        value from -1.0 to +1.0
     * @throws BasicPlayerException
     */
    public void setPan(double pan) throws BasicPlayerException {
        if (currentPlayer != null) currentPlayer.setPan(pan);
    }

    /**
     * Sets Gain value. Linear scale 0.0 <--> 1.0
     * 
     * @param gain
     *        value from 0.0 to 1.0
     * @throws BasicPlayerException
     */
    public void setGain(double gain) throws BasicPlayerException {

        if (currentPlayer != null) System.out.println("in controller.setgain");
        currentPlayer.setGain(gain);
    }

    public void addBasicPlayerListener(BasicPlayerListener bpl) {
        mp3Player.addBasicPlayerListener(bpl);
        midiKarPlayer.addBasicPlayerListener(bpl);
    }

    /**
     * Composite player supports everything mp3Player and midiKarPlayer supports
     * plus zip files(zipped mp3g).
     */
    public void setSupportedFileTypeExtensions() {
        String[] tab1 = mp3Player.getSupportedFileTypeExtensions();
        String[] tab2 = midiKarPlayer.getSupportedFileTypeExtensions();
        supportedFileTypeExtensions = new String[tab1.length + tab2.length + 1];

        int j = 0;
        for (int i = 0; i < tab1.length; i++) {
            // System.out.println("1 - supportedFileTypeExtensions["+j+"] = " +
            // tab1[i]);
            supportedFileTypeExtensions[j++] = tab1[i];
        }

        for (int i = 0; i < tab2.length; i++) {
            // System.out.println("2 - supportedFileTypeExtensions["+j+"] = " +
            // tab2[i]);
            supportedFileTypeExtensions[j++] = tab2[i];
        }

        supportedFileTypeExtensions[j++] = ".zip";
    }

    public void setPlayerUI(Iconifiable ui) {
        this.playerUI = ui;
        mp3Player.setPlayerUI(ui);
        midiKarPlayer.setPlayerUI(ui);
    }

    public Iconifiable getPlayerUI() {
        return playerUI;
    }
}
