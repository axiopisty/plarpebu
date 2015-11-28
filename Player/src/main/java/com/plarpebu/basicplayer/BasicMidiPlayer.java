/**
 * BasicMidiPlayer.
 * <p>
 * -----------------------------------------------------------------------
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * ----------------------------------------------------------------------
 * Author : Michel Buffa (buffa@unice.fr)
 */

package com.plarpebu.basicplayer;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.plarpebu.javakarplayer.plugins.AudioPlugins.JavaSoundMidiKar;
import com.plarpebu.javakarplayer.plugins.AudioPlugins.MidiListener;
import com.plarpebu.plugins.sdk.Iconifiable;

public class BasicMidiPlayer extends BasicPlayer implements MidiListener {

  private Object m_dataSource;

  private static Log log = LogFactory.getLog(BasicMP3Player.class);

  // For midi / kar play
  private JavaSoundMidiKar midiKarPlayer = new JavaSoundMidiKar();

  private Map emptyMap = new HashMap();

  /**
   * Constructs a Basic Player.
   */
  private long currentPosition = 0;

  private String artist;

  private String songTitle;

  public BasicMidiPlayer() {
    midiKarPlayer.setMidiListener(this);
    setSupportedFileTypeExtensions();
    reset();
  }

  protected void reset() {
    m_status = UNKNOWN;
  }

  /**
   * Redefine inherited method in roder to pass the playerUI to the midiKarPlayer
   *
   * @param playerUI
   */
  public void setPlayerUI(Iconifiable playerUI) {
    super.setPlayerUI(playerUI);
    midiKarPlayer.setPlayerUI(playerUI);
  }

  /**
   * Open file to play.
   */
  public void open(File file) throws BasicPlayerException {
    log.debug("open(" + file + ")");
    if(file != null) {
      if(file.getName().toLowerCase().endsWith(".kar") || file.getName().toLowerCase().endsWith(".mid")) {
        System.out.println("We got a midi or a kar file !");
        m_dataSource = file;
        initAudioInputStream();
        // midiKarPlayer.loadFile(file);
      }
    }
  }

  /**
   * Open URL to play.
   */
  public void open(URL url) throws BasicPlayerException {
    log.debug("open(" + url + ")");
    System.out.println("Not implemented");
  }

  /**
   * Open inputstream to play.
   */
  public void open(InputStream inputStream) throws BasicPlayerException {
    log.debug("open(" + inputStream + ")");
    System.out.println("Not implemented");
  }

  /**
   * Inits AudioInputStream and AudioFileFormat from the data source.
   *
   * @throws BasicPlayerException
   */
  private void initAudioInputStream() {

    reset();
    notifyEvent(BasicPlayerEvent.OPENING, getEncodedStreamPosition(), -1, m_dataSource);
    if(m_dataSource instanceof URL) {
      initAudioInputStream((URL) m_dataSource);
    } else if(m_dataSource instanceof File) {
      initAudioInputStream((File) m_dataSource);
    } else if(m_dataSource instanceof InputStream) {
      initAudioInputStream((InputStream) m_dataSource);
    }
    Map properties = new HashMap();
    // Add JavaSound properties.
    // properties.put("audio.type", "midi");

    // So that Album Grabber will try to get a cover, even with midi
    // files...
    properties.put("author", artist);
    properties.put("album", songTitle);

    properties.put("audio.length.bytes", new Integer((int) midiKarPlayer.getSongLength()));
    properties.put("duration", new Long((int) midiKarPlayer.getSongMicrosecondLength()));

    // And notify the listeners
    Iterator it = m_listeners.iterator();
    while(it.hasNext()) {
      BasicPlayerListener bpl = (BasicPlayerListener) it.next();
      bpl.opened(m_dataSource, properties);
    }

    m_status = OPENED;
    notifyEvent(BasicPlayerEvent.OPENED, getEncodedStreamPosition(), -1, null);

  }

  /**
   * Inits Audio ressources from file.
   */
  private void initAudioInputStream(File file) {
    midiKarPlayer.loadFile(file);
    artist = midiKarPlayer.getArtist();
    songTitle = midiKarPlayer.getSongTitle();
  }

  /**
   * Inits Audio ressources from URL.
   */
  private void initAudioInputStream(URL url) {
  }

  /**
   * Inits Audio ressources from InputStream.
   */
  private void initAudioInputStream(InputStream inputStream) {
  }

  /**
   * Called by the midi engine as song is being played
   */
  public void setCurrentPosition(long pos) {
    this.currentPosition = pos;

    // Call the listeners
    Iterator it = m_listeners.iterator();
    while(it.hasNext()) {
      BasicPlayerListener bpl = (BasicPlayerListener) it.next();

      bpl.progress((int) currentPosition, 0, null, emptyMap);
    }

  }

  /**
   * Stops the playback.<br>
   * Player Status = STOPPED.<br>
   * Thread should free Audio ressources.
   */
  protected void stopPlayback() {

    if((m_status == PLAYING) || (m_status == PAUSED)) {
      m_status = STOPPED;
      notifyEvent(BasicPlayerEvent.STOPPED, getEncodedStreamPosition(), -1, null);
      midiKarPlayer.stop();
      log.info("stopPlayback() completed");
    }
  }

  /**
   * Pauses the playback.<br>
   * Player Status = PAUSED.
   */
  protected void pausePlayback() {
    if(m_status == PLAYING) {
      m_status = PAUSED;
      log.info("pausePlayback() completed");
      notifyEvent(BasicPlayerEvent.PAUSED, getEncodedStreamPosition(), -1, null);
      midiKarPlayer.pause();
    }
  }

  /**
   * Resumes the playback.<br>
   * Player Status = PLAYING.
   */
  protected void resumePlayback() {
    if(m_status == PAUSED) {
      m_status = PLAYING;
      log.info("resumePlayback() completed");
      notifyEvent(BasicPlayerEvent.RESUMED, getEncodedStreamPosition(), -1, null);
      midiKarPlayer.resume();
    }
  }

  /**
   * Starts playback.
   */
  protected void startPlayback() {
    midiKarPlayer.play();
    m_status = PLAYING;
    notifyEvent(BasicPlayerEvent.PLAYING, getEncodedStreamPosition(), -1, null);
  }

  protected int getEncodedStreamPosition() {
    return (int) midiKarPlayer.getSongLength();
  }

  /**
   * Returns true if Gain control is supported.
   */
  public boolean hasGainControl() {
    return true;
  }

  /**
   * Returns Gain value.
   */
  public float getGainValue() {
    return 0;
  }

  /**
   * Gets max Gain value.
   */
  public float getMaximumGain() {
    return 1;
  }

  /**
   * Gets min Gain value.
   */
  public float getMinimumGain() {
    return 0;
  }

  /**
   * Returns true if Pan control is supported.
   */
  public boolean hasPanControl() {
    return true;
  }

  /**
   * Returns Pan precision.
   */
  public float getPrecision() {
    return 0.0f;
  }

  /**
   * Returns Pan value.
   */
  public float getPan() {
    return 1;
  }

  /**
   * @see javazoom.jlgui.basicplayer.BasicController#seek(long)
   */
  public long seek(long bytes) throws BasicPlayerException {
    return midiKarPlayer.seek(bytes);
  }

  /**
   * @see javazoom.jlgui.basicplayer.BasicController#play()
   */
  public void play() throws BasicPlayerException {

    startPlayback();
  }

  /**
   * @see javazoom.jlgui.basicplayer.BasicController#stop()
   */
  public void stop() throws BasicPlayerException {
    stopPlayback();
  }

  /**
   * @see javazoom.jlgui.basicplayer.BasicController#pause()
   */
  public void pause() throws BasicPlayerException {
    pausePlayback();
  }

  /**
   * @see javazoom.jlgui.basicplayer.BasicController#resume()
   */
  public void resume() throws BasicPlayerException {
    resumePlayback();
  }

  /**
   * Sets Pan value. Linear scale : -1.0 <--> +1.0
   */
  public void setPan(double fPan) throws BasicPlayerException {
    midiKarPlayer.setPan(fPan);
  }

  /**
   * Sets Gain value. Linear scale 0.0 <--> 1.0 Threshold Coef. : 1/2 to avoid saturation.
   */
  public void setGain(double fGain) throws BasicPlayerException {
    midiKarPlayer.setGain(fGain);
  }

  public void setSupportedFileTypeExtensions() {
    supportedFileTypeExtensions = new String[] {
      ".mid",
      ".kar"
    };
  }

}
