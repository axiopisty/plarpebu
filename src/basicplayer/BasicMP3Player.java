/**
 * BasicMp3Player.
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerEventLauncher;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import javazoom.spi.PropertiesContainer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 * BasicPlayer is a threaded audio javazoom.jlgui.player.test.
 */
public class BasicMP3Player extends BasicPlayer implements BasicController, Runnable {
    private static final int EXTERNAL_BUFFER_SIZE = 4000 * 4;

    private static final int SKIP_INACCURACY_SIZE = 1200;

    private Thread m_thread = null;

    private Object m_dataSource;

    private AudioInputStream m_encodedaudioInputStream;

    private int encodedLength = -1;

    private AudioInputStream m_audioInputStream;

    private AudioFileFormat m_audioFileFormat;

    private SourceDataLine m_line;

    private FloatControl m_gainControl;

    private FloatControl m_panControl;

    private int lineBufferSize = -1;

    private long threadSleep = -1;

    private static Log log = LogFactory.getLog(BasicPlayer.class);

    /**
     * These variables are used to distinguish stopped, paused, playing states.
     * We need them to control Thread.
     */
    public static final int UNKNOWN = -1;

    public static final int PLAYING = 0;

    public static final int PAUSED = 1;

    public static final int STOPPED = 2;

    public static final int OPENED = 3;

    public static final int SEEKING = 4;

    private int m_status = UNKNOWN;

    // Listeners to be notified.
    private Collection m_listeners = null;

    private Map empty_map = new HashMap();

    /**
     * Constructs a Basic Player.
     */
    public BasicMP3Player() {
        setSupportedFileTypeExtensions();
        m_dataSource = null;
        m_listeners = new ArrayList();
        reset();
    }

    protected void reset() {
        m_status = UNKNOWN;
        if (m_audioInputStream != null) {
            synchronized (m_audioInputStream) {
                closeStream();
            }
        }
        m_audioInputStream = null;
        m_audioFileFormat = null;
        m_encodedaudioInputStream = null;
        encodedLength = -1;
        if (m_line != null) {
            m_line.stop();
            m_line.close();
            m_line = null;
        }
        m_gainControl = null;
        m_panControl = null;
    }

    public void addBasicPlayerListener(BasicPlayerListener bpl) {
        m_listeners.add(bpl);
    }

    /**
     * Set SourceDataLine buffer size. It affects audio latency. (the delay
     * between line.write(data) and real sound). Minimum value should be over
     * 10000 bytes.
     * 
     * @param size
     *        -1 means maximum buffer size available.
     */
    public void setLineBufferSize(int size) {
        lineBufferSize = size;
    }

    /**
     * Return SourceDataLine buffer size.
     * 
     * @return -1 maximum buffer size.
     */
    public int getLineBufferSize() {
        return lineBufferSize;
    }

    /**
     * Set thread sleep time. Default is -1 (no sleep time).
     * 
     * @param time
     *        in milliseconds.
     */
    public void setSleepTime(long time) {
        threadSleep = time;
    }

    /**
     * Return thread sleep time in milliseconds.
     * 
     * @return -1 means no sleep time.
     */
    public long getSleepTime() {
        return threadSleep;
    }

    /**
     * Returns BasicPlayer status.
     * 
     * @return status
     */
    public int getStatus() {
        return m_status;
    }

    /**
     * Open file to play.
     */
    public void open(File file) throws BasicPlayerException {
        log.info("open(" + file + ")");
        if (file != null) {
            m_dataSource = file;
            initAudioInputStream();
        }
    }

    /**
     * Open URL to play.
     */
    public void open(URL url) throws BasicPlayerException {
        log.info("open(" + url + ")");
        if (url != null) {
            m_dataSource = url;
            initAudioInputStream();
        }
    }

    /**
     * Open inputstream to play.
     */
    public void open(InputStream inputStream) throws BasicPlayerException {
        log.info("open(" + inputStream + ")");
        if (inputStream != null) {
            m_dataSource = inputStream;
            initAudioInputStream();
        }
    }

    /**
     * Inits AudioInputStream and AudioFileFormat from the data source.
     * 
     * @throws BasicPlayerException
     */
    private void initAudioInputStream() throws BasicPlayerException {
        try {
            reset();
            notifyEvent(BasicPlayerEvent.OPENING, getEncodedStreamPosition(), -1, m_dataSource);
            if (m_dataSource instanceof URL) {
                initAudioInputStream((URL) m_dataSource);
            }
            else if (m_dataSource instanceof File) {
                initAudioInputStream((File) m_dataSource);
            }
            else if (m_dataSource instanceof InputStream) {
                initAudioInputStream((InputStream) m_dataSource);
            }
            createLine();
            // Notify listeners with AudioFileFormat properties.
            Map properties = null;
            if (m_audioFileFormat instanceof TAudioFileFormat) {
                // Tritonus SPI compliant audio file format.
                properties = ((TAudioFileFormat) m_audioFileFormat).properties();
                // Clone the Map because it is not mutable.
                properties = deepCopy(properties);
            }
            else
                properties = new HashMap();
            // Add JavaSound properties.
            if (m_audioFileFormat.getByteLength() > 0)
                properties.put("audio.length.bytes", new Integer(m_audioFileFormat.getByteLength()));
            if (m_audioFileFormat.getFrameLength() > 0)
                properties.put("audio.length.frames", new Integer(m_audioFileFormat.getFrameLength()));
            if (m_audioFileFormat.getType() != null)
                properties.put("audio.type", (m_audioFileFormat.getType().toString()));
            // Audio format.
            AudioFormat audioFormat = m_audioFileFormat.getFormat();
            if (audioFormat.getFrameRate() > 0)
                properties.put("audio.framerate.fps", new Float(audioFormat.getFrameRate()));
            if (audioFormat.getFrameSize() > 0)
                properties.put("audio.framesize.bytes", new Integer(audioFormat.getFrameSize()));
            if (audioFormat.getSampleRate() > 0)
                properties.put("audio.samplerate.hz", new Float(audioFormat.getSampleRate()));
            if (audioFormat.getSampleSizeInBits() > 0)
                properties.put("audio.samplesize.bits", new Integer(audioFormat.getSampleSizeInBits()));
            if (audioFormat.getChannels() > 0)
                properties.put("audio.channels", new Integer(audioFormat.getChannels()));
            if (audioFormat instanceof TAudioFormat) {
                // Tritonus SPI compliant audio format.
                Map addproperties = ((TAudioFormat) audioFormat).properties();
                properties.putAll(addproperties);
            }
            Iterator it = m_listeners.iterator();
            while (it.hasNext()) {
                BasicPlayerListener bpl = (BasicPlayerListener) it.next();
                bpl.opened(m_dataSource, properties);
            }
            m_status = OPENED;
            notifyEvent(BasicPlayerEvent.OPENED, getEncodedStreamPosition(), -1, null);
        }
        catch (LineUnavailableException e) {
            throw new BasicPlayerException(e);
        }
        catch (UnsupportedAudioFileException e) {
            throw new BasicPlayerException(e);
        }
        catch (IOException e) {
            throw new BasicPlayerException(e);
        }
    }

    /**
     * Inits Audio ressources from file.
     */
    private void initAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        m_audioInputStream = AudioSystem.getAudioInputStream(file);
        m_audioFileFormat = AudioSystem.getAudioFileFormat(file);
    }

    /**
     * Inits Audio ressources from URL.
     */
    private void initAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        m_audioInputStream = AudioSystem.getAudioInputStream(url);
        m_audioFileFormat = AudioSystem.getAudioFileFormat(url);
    }

    /**
     * Inits Audio ressources from InputStream.
     */
    private void initAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        m_audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        m_audioFileFormat = AudioSystem.getAudioFileFormat(inputStream);
    }

    /**
     * Inits Audio ressources from AudioSystem.<br>
     */
    protected void initLine() throws LineUnavailableException {
        log.info("initLine()");
        if (m_line == null) createLine();
        if (!m_line.isOpen()) {
            openLine();
        }
        else {
            AudioFormat lineAudioFormat = m_line.getFormat();
            AudioFormat audioInputStreamFormat = m_audioInputStream == null ? null : m_audioInputStream.getFormat();
            if (!lineAudioFormat.equals(audioInputStreamFormat)) {
                m_line.close();
                openLine();
            }
        }
    }

    /**
     * Inits a DateLine.<br>
     * We check if the line supports Gain and Pan controls. From the
     * AudioInputStream, i.e. from the sound file, we fetch information about
     * the format of the audio data. These information include the sampling
     * frequency, the number of channels and the size of the samples. There
     * information are needed to ask JavaSound for a suitable output line for
     * this audio file. Furthermore, we have to give JavaSound a hint about how
     * big the internal buffer for the line should be. Here, we say
     * AudioSystem.NOT_SPECIFIED, signaling that we don't care about the exact
     * size. JavaSound will use some default value for the buffer size.
     */
    private void createLine() throws LineUnavailableException {
        log.info("Create Line");
        if (m_line == null) {
            AudioFormat sourceFormat = m_audioInputStream.getFormat();
            log.info("Create Line : Source format : " + sourceFormat.toString());
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(),
            16, sourceFormat.getChannels(), sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);
            log.info("Create Line : Target format: " + targetFormat);
            // Keep a reference on encoded stream to progress notification.
            m_encodedaudioInputStream = m_audioInputStream;
            try {
                // Get total length in bytes of the encoded stream.
                encodedLength = m_encodedaudioInputStream.available();
            }
            catch (IOException e) {
                log.error("Cannot get m_encodedaudioInputStream.available()", e);
            }
            // Create decoded stream.
            m_audioInputStream = AudioSystem.getAudioInputStream(targetFormat, m_audioInputStream);
            AudioFormat audioFormat = m_audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
            m_line = (SourceDataLine) AudioSystem.getLine(info);

            /*-- Display supported controls --*/
            Control[] c = m_line.getControls();
            for (int p = 0; p < c.length; p++) {
                log.debug("Controls : " + c[p].toString());
            }

            /*-- Is Gain Control supported ? --*/
            if (m_line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                m_gainControl = (FloatControl) m_line.getControl(FloatControl.Type.MASTER_GAIN);
                log.info("Master Gain Control : [" + m_gainControl.getMinimum() + "," + m_gainControl.getMaximum()
                + "] " + m_gainControl.getPrecision());
            }

            /*-- Is Pan control supported ? --*/
            if (m_line.isControlSupported(FloatControl.Type.PAN)) {
                m_panControl = (FloatControl) m_line.getControl(FloatControl.Type.PAN);
                log.info("Pan Control : [" + m_panControl.getMinimum() + "," + m_panControl.getMaximum() + "] "
                + m_panControl.getPrecision());
            }
        }
    }

    /**
     * Opens the line.
     */
    private void openLine() throws LineUnavailableException {
        if (m_line != null) {
            AudioFormat audioFormat = m_audioInputStream.getFormat();
            int buffersize = lineBufferSize;
            if (buffersize <= 0) buffersize = m_line.getBufferSize();
            m_line.open(audioFormat, buffersize);
            log.info("Open Line : BufferSize=" + buffersize);
        }
    }

    /**
     * Stops the playback.<br>
     * Player Status = STOPPED.<br>
     * Thread should free Audio ressources.
     */
    protected void stopPlayback() {
        if ((m_status == PLAYING) || (m_status == PAUSED)) {
            if (m_line != null) {
                m_line.flush();
                m_line.stop();
            }
            m_status = STOPPED;
            notifyEvent(BasicPlayerEvent.STOPPED, getEncodedStreamPosition(), -1, null);
            synchronized (m_audioInputStream) {
                closeStream();
            }
            log.info("stopPlayback() completed");
        }
    }

    /**
     * Pauses the playback.<br>
     * Player Status = PAUSED.
     */
    protected void pausePlayback() {
        if (m_line != null) {
            if (m_status == PLAYING) {
                m_line.flush();
                m_line.stop();
                m_status = PAUSED;
                log.info("pausePlayback() completed");
                notifyEvent(BasicPlayerEvent.PAUSED, getEncodedStreamPosition(), -1, null);
            }
        }
    }

    /**
     * Resumes the playback.<br>
     * Player Status = PLAYING.
     */
    protected void resumePlayback() {
        if (m_line != null) {
            if (m_status == PAUSED) {
                m_line.start();
                m_status = PLAYING;
                log.info("resumePlayback() completed");
                notifyEvent(BasicPlayerEvent.RESUMED, getEncodedStreamPosition(), -1, null);
            }
        }
    }

    /**
     * Starts playback.
     */
    protected void startPlayback() throws BasicPlayerException {
        if (m_status == STOPPED) initAudioInputStream();
        if (m_status == OPENED) {
            log.info("startPlayback called");
            if (!(m_thread == null || !m_thread.isAlive())) {
                log.info("WARNING: old thread still running!!");
                int cnt = 0;
                while (m_status != OPENED) {
                    try {
                        if (m_thread != null) {
                            log.info("Waiting ... " + cnt);
                            cnt++;
                            Thread.sleep(1000);
                            if (cnt > 2) {
                                m_thread.interrupt();
                            }
                        }
                    }
                    catch (InterruptedException e) {
                        throw new BasicPlayerException("Wait error", e);
                    }
                }
            }
            // Open SourceDataLine.
            try {
                initLine();
            }
            catch (LineUnavailableException e) {
                throw new BasicPlayerException("Cannot init line", e);
            }

            log.info("Creating new thread");
            m_thread = new Thread(this);
            m_thread.start();
            if (m_line != null) {
                m_line.start();
                m_status = PLAYING;
                notifyEvent(BasicPlayerEvent.PLAYING, getEncodedStreamPosition(), -1, null);
            }
        }
    }

    /**
     * Main loop. Player Status == STOPPED || SEEKING => End of Thread + Freeing
     * Audio Ressources.<br>
     * Player Status == PLAYING => Audio stream data sent to Audio line.<br>
     * Player Status == PAUSED => Waiting for another status.
     */
    public void run() {
        log.info("Thread Running");
        int nBytesRead = 1;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
        // Lock stream while playing.
        synchronized (m_audioInputStream) {
            // Main play/pause loop.
            while ((nBytesRead != -1) && (m_status != STOPPED) && (m_status != SEEKING) && (m_status != UNKNOWN)) {
                if (m_status == PLAYING) {
                    // Play.
                    try {
                        nBytesRead = m_audioInputStream.read(abData, 0, abData.length);
                        if (nBytesRead >= 0) {
                            byte[] pcm = new byte[nBytesRead];
                            System.arraycopy(abData, 0, pcm, 0, nBytesRead);
                            int nBytesWritten = m_line.write(abData, 0, nBytesRead);
                            // Compute position in bytes in encoded stream.
                            int nEncodedBytes = getEncodedStreamPosition();
                            // Notify listeners
                            Iterator it = m_listeners.iterator();
                            while (it.hasNext()) {
                                BasicPlayerListener bpl = (BasicPlayerListener) it.next();
                                if (m_audioInputStream instanceof PropertiesContainer) {
                                    // Pass audio parameters such as instant
                                    // bitrate, ...
                                    Map properties = ((PropertiesContainer) m_audioInputStream).properties();
                                    bpl.progress(nEncodedBytes, m_line.getMicrosecondPosition(), pcm, properties);
                                }
                                else
                                    bpl.progress(nEncodedBytes, m_line.getMicrosecondPosition(), pcm, empty_map);
                            }
                        }
                    }
                    catch (IOException e) {
                        log.error("Thread cannot run()", e);
                        m_status = STOPPED;
                        notifyEvent(BasicPlayerEvent.STOPPED, getEncodedStreamPosition(), -1, null);
                    }
                    // Nice CPU usage.
                    if (threadSleep > 0) {
                        try {
                            Thread.sleep(threadSleep);
                        }
                        catch (InterruptedException e) {
                            log.error("Thread cannot sleep(" + threadSleep + ")", e);
                        }
                    }
                }
                else {
                    // Pause
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        log.error("Thread cannot sleep(1000)", e);
                    }
                }
            }
            // Free audio resources.
            if (m_line != null) {
                m_line.drain();
                m_line.stop();
                m_line.close();
                m_line = null;
            }
            // Notification of "End Of Media"
            if (nBytesRead == -1) {
                notifyEvent(BasicPlayerEvent.EOM, getEncodedStreamPosition(), -1, null);
            }
            // Close stream.
            closeStream();
        }
        m_status = STOPPED;
        notifyEvent(BasicPlayerEvent.STOPPED, getEncodedStreamPosition(), -1, null);
        log.info("Thread completed");
    }

    /**
     * Skip bytes in the File inputstream. It will skip N frames matching to
     * bytes, so it will never skip given bytes length exactly.
     * 
     * @param bytes
     * @return value>0 for File and value=0 for URL and InputStream
     * @throws BasicPlayerException
     */
    protected long skipBytes(long bytes) throws BasicPlayerException {
        long totalSkipped = 0;
        if (m_dataSource instanceof File) {
            log.info("Bytes to skip : " + bytes);
            int previousStatus = m_status;
            m_status = SEEKING;
            long skipped = 0;
            try {
                synchronized (m_audioInputStream) {
                    notifyEvent(BasicPlayerEvent.SEEKING, getEncodedStreamPosition(), -1, null);
                    initAudioInputStream();
                    if (m_audioInputStream != null) {
                        // Loop until bytes are really skipped.
                        while (totalSkipped < (bytes - SKIP_INACCURACY_SIZE)) {
                            skipped = m_audioInputStream.skip(bytes - totalSkipped);
                            totalSkipped = totalSkipped + skipped;
                            log.info("Skipped : " + totalSkipped + "/" + bytes);
                            if (totalSkipped == -1) throw new BasicPlayerException("Skip not supported");
                        }
                    }
                }
                notifyEvent(BasicPlayerEvent.SEEKED, getEncodedStreamPosition(), -1, null);
                m_status = OPENED;
                if (previousStatus == PLAYING)
                    startPlayback();
                else if (previousStatus == PAUSED) {
                    startPlayback();
                    pausePlayback();
                }
            }
            catch (IOException e) {
                throw new BasicPlayerException(e);
            }
        }
        return totalSkipped;
    }

    /**
     * Notify listeners about a BasicPlayerEvent.
     * 
     * @param code
     *        event code.
     * @param position
     *        in the stream when the event occurs.
     */
    protected void notifyEvent(int code, int position, double value, Object description) {
        /*
         * Iterator it = m_listeners.iterator(); while (it.hasNext()) {
         * BasicPlayerListener bpl = (BasicPlayerListener) it.next();
         * bpl.stateUpdated(new
         * BasicPlayerEvent(this,code,position,value,description)); }
         */
        BasicPlayerEventLauncher trigger = new BasicPlayerEventLauncher(code, position, value, description,
        m_listeners, this);
        trigger.start();
    }

    protected int getEncodedStreamPosition() {
        int nEncodedBytes = -1;
        if (m_dataSource instanceof File) {
            try {
                if (m_encodedaudioInputStream != null) {
                    nEncodedBytes = encodedLength - m_encodedaudioInputStream.available();
                }
            }
            catch (IOException e) {
                // log.debug("Cannot get
                // m_encodedaudioInputStream.available()",e);
            }
        }
        return nEncodedBytes;
    }

    protected void closeStream() {
        // Close stream.
        try {
            if (m_audioInputStream != null) {
                m_audioInputStream.close();
                log.info("Stream closed");
            }

        }
        catch (IOException e) {
            log.info("Cannot close stream", e);
        }
    }

    /**
     * Returns true if Gain control is supported.
     */
    public boolean hasGainControl() {
        return m_gainControl != null;
    }

    /**
     * Returns Gain value.
     */
    public float getGainValue() {
        if (hasGainControl()) {
            return m_gainControl.getValue();
        }
        else {
            return 0.0F;
        }
    }

    /**
     * Gets max Gain value.
     */
    public float getMaximumGain() {
        if (hasGainControl()) {
            return m_gainControl.getMaximum();
        }
        else {
            return 0.0F;
        }
    }

    /**
     * Gets min Gain value.
     */
    public float getMinimumGain() {
        if (hasGainControl()) {
            return m_gainControl.getMinimum();
        }
        else {
            return 0.0F;
        }
    }

    /**
     * Returns true if Pan control is supported.
     */
    public boolean hasPanControl() {
        return m_panControl != null;
    }

    /**
     * Returns Pan precision.
     */
    public float getPrecision() {
        if (hasPanControl()) {
            return m_panControl.getPrecision();
        }
        else {
            return 0.0F;
        }
    }

    /**
     * Returns Pan value.
     */
    public float getPan() {
        if (hasPanControl()) {
            return m_panControl.getValue();
        }
        else {
            return 0.0F;
        }
    }

    /**
     * Deep copy of a Map.
     * 
     * @param src
     * @return
     */
    private Map deepCopy(Map src) {
        HashMap map = new HashMap();
        if (src != null) {
            Iterator it = src.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object value = src.get(key);
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * @see javazoom.jlgui.basicplayer.BasicController#seek(long)
     */
    public long seek(long bytes) throws BasicPlayerException {
        return skipBytes(bytes);
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
        if (hasPanControl()) {
            log.debug("Pan : " + fPan);
            m_panControl.setValue((float) fPan);
            notifyEvent(BasicPlayerEvent.PAN, getEncodedStreamPosition(), fPan, null);
        }
        else
            throw new BasicPlayerException("Pan control not supported");
    }

    /**
     * Sets Gain value. Linear scale 0.0 <--> 1.0 Threshold Coef. : 1/2 to avoid
     * saturation.
     */
    public void setGain(double fGain) throws BasicPlayerException {
        if (hasGainControl()) {
            double minGainDB = getMinimumGain();
            double ampGainDB = ((10.0f / 20.0f) * getMaximumGain()) - getMinimumGain();
            double cste = Math.log(10.0) / 20;
            double valueDB = minGainDB + (1 / cste) * Math.log(1 + (Math.exp(cste * ampGainDB) - 1) * fGain);
            log.debug("Gain : " + valueDB);
            m_gainControl.setValue((float) valueDB);
            notifyEvent(BasicPlayerEvent.GAIN, getEncodedStreamPosition(), fGain, null);
        }
        else
            throw new BasicPlayerException("Gain control not supported");
    }

    public void setSupportedFileTypeExtensions() {
        supportedFileTypeExtensions = new String[] { ".mp3", ".ogg" };
    }
}