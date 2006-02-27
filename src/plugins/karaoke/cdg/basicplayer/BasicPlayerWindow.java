package plugins.karaoke.cdg.basicplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.Timer;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import plugins.karaoke.cdg.basicplayer.gui.CdgOptionsDialog;
import plugins.karaoke.cdg.basicplayer.gui.ChooseFullScreenModeDIalog;
import plugins.karaoke.cdg.instructions.CdgBorderPreset;
import plugins.karaoke.cdg.instructions.CdgLoadColorTable;
import plugins.karaoke.cdg.instructions.CdgMemoryPreset;
import plugins.karaoke.cdg.instructions.CdgScrollCopy;
import plugins.karaoke.cdg.instructions.CdgScrollPreset;
import plugins.karaoke.cdg.instructions.CdgTileBlock;
import plugins.karaoke.cdg.io.CdgDataChunk;
import plugins.karaoke.cdg.io.CdgFileObject;
import plugins.karaoke.cdg.lyricspanel.CdgGraphicBufferedImage;
import pluginsSDK.FramePlugin;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Michel Buffa (buffa@unice.fr)
 * @version $Id
 */

public class BasicPlayerWindow extends FramePlugin implements BasicPlayerListener {

    private long tempsMp3;

    private BasicController controllerMp3;

    private Timer timer;

    private int current_row;

    private CdgDataChunk[] cdgDataChunksArray;

    private Color[] colormap = new Color[16];

    private Rectangle damagedRectangle;

    private CdgGraphicBufferedImage panelLyrics = new CdgGraphicBufferedImage();

    private boolean windowedMode = true;

    private boolean cdgFileLoaded = false;

    private boolean pausedPlay = false;

    private boolean seeking = false;

    private int nbCdgInstructions = 10;

    // For full screen mode
    private DisplayMode userDisplayMode = null;

    private int oldWindowDecorationStyle;

    private static DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[] { new DisplayMode(640, 480, 32, 0),
    new DisplayMode(640, 480, 16, 0), new DisplayMode(640, 480, 8, 0) };

    private GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

    private GraphicsDevice myDevice = env.getDefaultScreenDevice();

    private DisplayMode oldDisplayMode = myDevice.getDisplayMode();

    // private JLabel jLabel1 = new JLabel("Status area...");
    JPopupMenu jPopupMenu1 = new JPopupMenu();

    JMenuItem jMenuItemHelp = new JMenuItem();

    JMenuItem jMenuItemFullScreensOptions = new JMenuItem();

    JMenuItem jMenuItemLyricsSyncOptions = new JMenuItem();

    ChooseFullScreenModeDIalog chooseFullScreenDialog;

    CdgOptionsDialog cdgOptionsDialog;

    JMenuItem jMenuItemFullScreenWindow = new JMenuItem();

    // Construct the frame
    private int oldPosX;

    private int oldPosY;

    private int oldWidth;

    private int oldHeight;

    private boolean hardwareAcceleratedFullScreenModeSupported = false;

    JCheckBoxMenuItem jCheckBoxMenuItem2 = new JCheckBoxMenuItem();

    public BasicPlayerWindow() {
        setTitle("CDG Lyrics Display " + getVersion());
        // the mp3 player
        setUndecorated(true);
        // setIgnoreRepaint(true);
        // setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Container cp = getContentPane();
        cp.add(panelLyrics, BorderLayout.CENTER);
        // cp.add(jLabel1, BorderLayout.SOUTH);
        pack();
        // addMouseMotionListener(new MouseDragger());
        addMouseListener(new MyMouseListener());
        addKeyListener(new MyKeyListener());

        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
        // Dialog for chosing the full screen mode, modal
        chooseFullScreenDialog = new ChooseFullScreenModeDIalog(this, true);
        cdgOptionsDialog = new CdgOptionsDialog(this, "Cdg options", true);

        loadPreferences();
    }

    /**
     * We redefine the inherited method so that visibility is always set to
     * false before saving the preferences
     */
    public void savePreferences() {
        System.out.println("dans pref redéfinie");
        setVisible(false);
        super.savePreferences();
    }

    private void loadPreferences() throws NumberFormatException, HeadlessException {
        // inherited behavior
        readPreferences();

        try {
            // Read all preferences
            String stringValue = null;
            Boolean value = null;

            if ((stringValue = preferences.getProperty("nbCdgInstructions")) != null) {
                setNbCdgInstructions(Integer.parseInt(stringValue));
                cdgOptionsDialog.setCdgBufferSize(Integer.parseInt(stringValue));
            }

            if ((stringValue = preferences.getProperty("redrawAllFrame")) != null) {
                value = Boolean.valueOf(stringValue);
                panelLyrics.setForceDrawFullImage(value.booleanValue());
                cdgOptionsDialog.setRedrawFullImage(value.booleanValue());
            }

            if ((stringValue = preferences.getProperty("hardwareAcceleratedFullScreenModeSupported")) != null) {
                value = Boolean.valueOf(stringValue);
                hardwareAcceleratedFullScreenModeSupported = value.booleanValue();
                setFullScreenModeOption();
            }

            int fsWidth = -1, fsHeight = -1, fsDepth = -1, fsFreq = -1;
            if ((stringValue = preferences.getProperty("fsWidth")) != null) {
                fsWidth = Integer.parseInt(stringValue);
            }
            if ((stringValue = preferences.getProperty("fsHeight")) != null) {
                fsHeight = Integer.parseInt(stringValue);
            }
            if ((stringValue = preferences.getProperty("fsDepth")) != null) {
                fsDepth = Integer.parseInt(stringValue);
            }
            if ((stringValue = preferences.getProperty("fsFreq")) != null) {
                fsFreq = Integer.parseInt(stringValue);
            }

            if ((fsWidth != -1) && (fsHeight != -1) && (fsDepth != -1) && (fsFreq != -1)) {
                System.out.println("Read prefs for fs mode, plug " + getName() + " : (" + fsWidth + "," + fsHeight
                + "," + fsDepth + "," + fsFreq + ")");
                userDisplayMode = new DisplayMode(fsWidth, fsHeight, fsDepth, fsFreq);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The window will appear centered
    /*
     * public void setVisible(boolean flag) { Dimension screenSize =
     * Toolkit.getDefaultToolkit().getScreenSize(); Dimension frameSize =
     * getSize(); if (frameSize.height > screenSize.height) { frameSize.height =
     * screenSize.height; } if (frameSize.width > screenSize.width) {
     * frameSize.width = screenSize.width; } setLocation( (screenSize.width -
     * frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
     * super.setVisible(flag); }
     */
    /*
     * public void play(File file, int nbCdgInstructions) { try { if (player ==
     * null) { player = new BasicPlayer(this); } System.out.println("Opening mp3
     * file : " + file.getAbsolutePath()); player.setDataSource(new
     * File(file.getAbsolutePath())); //player.setDataSource(new
     * File(file.getAbsolutePath())); loadCdgFile(file); cdgFileLoaded = true;
     * player.startPlayback(); startTimedPlay(); } catch (Exception ex) {
     * cdgFileLoaded = false; ex.printStackTrace(); } }
     */
    /*
     * public void playCdgOnly(int nbCdgInstructions) { if (cdgFileLoaded)
     * startTimedPlay(); }
     */

    public String getName() {
        return "KaraokeCdg";
    }

    public String getVersion() {
        return "V1.0 beta 21";
    }

    public void stopCdgOnly() {

        if (timer != null) {
            setVisible(false);
            timer.stop();
            cdgFileLoaded = false;
        }
    }

    public void setNbCdgInstructions(int nbCdgInstructions) {
        this.nbCdgInstructions = nbCdgInstructions;
    }

    /*
     * public void stopMp3PlusCdg() { stopCdgOnly(); player.stopPlayback(); }
     */
    /**
     * Loads a cdg file whose basename is taken from the mp3File parameter
     * 
     * @param mp3File :
     *        the name of the mp3File, its basename will be used for getting the
     *        cdg file associated
     */
    public void loadCdgFile(File mp3File) {
        try {
            int length = mp3File.getAbsolutePath().length();
            String cdgFileName = mp3File.getAbsolutePath().substring(0, length - 4) + ".cdg";
            File f = new File(cdgFileName);
            if (f.exists()) {
                System.out.println("Opening cdg file : " + cdgFileName);
                CdgFileObject cdg = new CdgFileObject(cdgFileName);
                cdgDataChunksArray = cdg.getCdgDataChunksArray();
                cdgFileLoaded = true;
                setVisible(true);
            }
            else {
                // No cdg file associated
                cdgFileLoaded = false;
                setVisible(false);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
            cdgFileLoaded = false;
            setVisible(false);
        }
    }

    private void startTimedPlay() throws NumberFormatException {

        if (timer != null) timer.stop();

        current_row = 0;

        final int delay = (int) (nbCdgInstructions * 3.33);

        System.out.println("---We launch timer with delay = " + (nbCdgInstructions * 3.33) + "---");
        // each cdg instruction lasts 0.00333333 s
        timer = new Timer(delay, new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (!pausedPlay) {
                    for (int i = 0; i < nbCdgInstructions; i++) {
                        playCdgInstruction();
                        current_row++;
                    }

                    // After we played the buffer of instructions, do the
                    // "asservissement"
                    if (tempsMp3 != 0) {
                        if ((current_row * 3.33) > (tempsMp3 + 100)) {
                            // we check if previously we were speeding up...
                            if (timer.getDelay() < delay) {
                                // we were speeding up, let's go back to the
                                // "normal" value
                                timer.setDelay(delay);
                                // jLabel1.setText("standard delay\t\t " +
                                // (int)(current_row * 3.33) + "\t>\t" +
                                // tempsMp3 + "\t\tdelay : " +
                                // timer.getDelay());
                            }
                            else {
                                // let's continue slowing down
                                // System.out.println("ralentit " + (current_row
                                // * 3.33) + " > " + tempsMp3);
                                timer.setDelay(timer.getDelay() + 100);
                                // jLabel1.setText("slowing down\t\t " +
                                // (int)(current_row * 3.33) + "\t>\t" +
                                // tempsMp3 + "\t\tdelay : " +
                                // timer.getDelay());
                            }

                        }
                        else if ((current_row * 3.33) < (tempsMp3 - 100)) {
                            // we check if previously we were slowing down...
                            if (timer.getDelay() > delay) {
                                // we were slowing down, let's go back to the
                                // "normal" value
                                timer.setDelay(delay);
                                // jLabel1.setText("standard delay\t\t " +
                                // (int)(current_row * 3.33) + "\t>\t" +
                                // tempsMp3 + "\t\tdelay : " +
                                // timer.getDelay());
                            }
                            else {

                                // System.out.println("accélère " + (current_row
                                // * 3.33) + " < " + tempsMp3);
                                if (timer.getDelay() > 0) {
                                    timer.setDelay(timer.getDelay() - 1);
                                    // jLabel1.setText("speeding up\t\t " +
                                    // (int) (current_row * 3.33) +
                                    // "\t>\t" + tempsMp3 + "\t\tdelay : " +
                                    // timer.getDelay());
                                }
                            }
                        }
                    }

                }
            }
        });
        timer.start();
    }

    public void playCdgInstruction() {
        boolean ret = false;

        if (cdgDataChunksArray[current_row].getCdgInstruction() == CdgDataChunk.CDG_LOAD_COL_TABLE_LOW) {
            // Allocate the colors 0-7 of the colormap
            CdgLoadColorTable.setColormap(cdgDataChunksArray[current_row].getCdgData(), 0, colormap);
            // panelColormap.setColormap(colormap);
            panelLyrics.setColormapLow(colormap);
        }
        else if (cdgDataChunksArray[current_row].getCdgInstruction() == CdgDataChunk.CDG_LOAD_COL_TABLE_HIGH) {
            // Allocate the colors 8-15 of the colormap
            CdgLoadColorTable.setColormap(cdgDataChunksArray[current_row].getCdgData(), 8, colormap);
            // panelColormap.setColormap(colormap);
            panelLyrics.setColormapHigh(colormap);
        }
        else if (cdgDataChunksArray[current_row].getCdgInstruction() == CdgDataChunk.CDG_TILE_NORMAL) {
            damagedRectangle = CdgTileBlock.drawTile(cdgDataChunksArray[current_row].getCdgData(), panelLyrics
            .getPixels(), false);
            panelLyrics.pixelsChanged(damagedRectangle);
        }
        else if (cdgDataChunksArray[current_row].getCdgInstruction() == CdgDataChunk.CDG_TILE_XOR) {
            damagedRectangle = CdgTileBlock.drawTile(cdgDataChunksArray[current_row].getCdgData(), panelLyrics
            .getPixels(), true);
            panelLyrics.pixelsChanged(damagedRectangle);
        }
        else if (cdgDataChunksArray[current_row].getCdgInstruction() == CdgDataChunk.CDG_MEMORY_PRESET) {
            if (CdgMemoryPreset.clearScreen(cdgDataChunksArray[current_row].getCdgData(), panelLyrics.getPixels())) {
                // if previous instructions returned false, the screen has
                // already
                // been cleared in a previous call...
                panelLyrics.pixelsChanged();
            }
        }
        else if (cdgDataChunksArray[current_row].getCdgInstruction() == CdgDataChunk.CDG_BORDER_PRESET) {
            CdgBorderPreset.drawBorder(cdgDataChunksArray[current_row].getCdgData(), panelLyrics.getPixels());

            panelLyrics.pixelsChanged();
        }
        else if (cdgDataChunksArray[current_row].getCdgInstruction() == CdgDataChunk.CDG_SCROLL_COPY) {
            panelLyrics.savePixels();
            ret = CdgScrollCopy.scroll(cdgDataChunksArray[current_row].getCdgData(), panelLyrics.getPixels());

            panelLyrics.pixelsChanged();
            if (!ret) panelLyrics.restorePixels();
        }
        else if (cdgDataChunksArray[current_row].getCdgInstruction() == CdgDataChunk.CDG_SCROLL_PRESET) {
            panelLyrics.savePixels();
            ret = CdgScrollPreset.scroll(cdgDataChunksArray[current_row].getCdgData(), panelLyrics.getPixels());

            panelLyrics.pixelsChanged();
            if (!ret) panelLyrics.restorePixels();
        }
    }

    // For full screen support
    public static void chooseBestDisplayMode(GraphicsDevice device) {
        DisplayMode best = getBestDisplayMode(device);
        if (best != null) {
            device.setDisplayMode(best);
        }
    }

    private static DisplayMode getBestDisplayMode(GraphicsDevice device) {
        for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
            DisplayMode[] modes = device.getDisplayModes();
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth()
                && modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight()
                && modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth()) {
                    return BEST_DISPLAY_MODES[x];
                }
            }
        }
        return null;
    }

    private void switchFullScreenWindowedMode() {
        if (windowedMode) {
            System.out.println("Go TO FULL SCREEN");
            // Go to full screen
            try {
                // go fullscreen
                goToFullScreen();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                goToWindowedMode();
            }
        }
        else {
            goToWindowedMode();
        }
    }

    public void displayDecorations(boolean flag) {
        dispose();
        // must be displayable (disposed) to call this...
        setUndecorated(!flag);
        pack();
    }

    private void goToFullScreen() {
        if (!hardwareAcceleratedFullScreenModeSupported) return;

        // Store old pos and size
        oldPosX = getX();
        oldPosY = getY();
        oldWidth = getWidth();
        oldHeight = getHeight();

        if (windowedMode) {
            // No borders, close buttons etc...
            // displayDecorations(false);
            oldWindowDecorationStyle = getRootPane().getWindowDecorationStyle();

            getRootPane().setWindowDecorationStyle(JRootPane.NONE);

            myDevice.setFullScreenWindow(BasicPlayerWindow.this);
            if (myDevice.isDisplayChangeSupported()) {
                panelLyrics.setWindowedMode(false);
                if (userDisplayMode == null) {
                    chooseBestDisplayMode(myDevice);
                }
                else {
                    myDevice.setDisplayMode(userDisplayMode);
                }
            }
            controllerMp3.getPlayerUI().minimize();
            // we can't modify full screen options while in full screen
            windowedMode = false;
        }
    }

    private void goToWindowedMode() {
        if (!hardwareAcceleratedFullScreenModeSupported) return;

        if (!windowedMode) {
            // Go back to the old display mode, windowed, with decorations...
            myDevice.setDisplayMode(oldDisplayMode);
            myDevice.setFullScreenWindow(null);
            panelLyrics.setWindowedMode(true);

            // restore decorations
            getRootPane().setWindowDecorationStyle(oldWindowDecorationStyle);

            restorePositionAndSize();
            setVisible(true);
            // !!!

            // de-iconify the main interface of the player
            controllerMp3.getPlayerUI().setToOriginalSize();

            // setUndecorated(true);
            // we can only modify full screen options while in window mode
            windowedMode = true;
        }
    }

    private void restorePositionAndSize() {
        // no hardware acceleration mode, just restore the window to its
        // previous pos and size
        setLocation(oldPosX, oldPosY);
        setSize(oldWidth, oldHeight);
    }

    public class MyMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                // Switching to/from fullScreen/windowed mode
                switchFullScreenWindowedMode();
            }
        }
    }

    public class MyKeyListener extends KeyAdapter {
        /**
         * Key pressed
         */
        public void keyPressed(KeyEvent e) {
            System.out.println("Key Event: " + e.getKeyCode());

            // If escape key is pressed, go to windowed mode!
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) goToWindowedMode();
        }
    }

    // ----------------------------------------------
    // Méthods required by the pluglin implementation
    // ----------------------------------------------
    /**
     * A handle to the BasicPlayer, plugins may control the player through the
     * controller (play, stop, etc...)
     * 
     * @param controller :
     *        a handle to the player
     */
    public void setController(BasicController controller) {
        this.controllerMp3 = controller;
    }

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
    public void opened(Object stream, Map properties) {
        if (!seeking) {
            String audiotype = null;

            if (stream != null) {
                if (stream instanceof File) {
                    System.out.println("File : " + ((File) stream).getAbsolutePath());
                    System.out.println("------------------");
                    System.out.println("Trying to Load cdg file...");
                    System.out.println("------------------");
                    loadCdgFile((File) stream);
                    if (!cdgFileLoaded) {
                        System.out.println("No Cdg file associated !");
                        return;
                    }

                    setVisible(true);
                }
                else if (stream instanceof URL) {
                    System.out.println("URL : " + ((URL) stream).toString());
                }
            }
            Iterator it = properties.keySet().iterator();
            StringBuffer jsSB = new StringBuffer();
            StringBuffer extjsSB = new StringBuffer();
            StringBuffer spiSB = new StringBuffer();
            if (properties.containsKey("audio.type")) {
                audiotype = ((String) properties.get("audio.type")).toLowerCase();
            }

            while (it.hasNext()) {
                String key = (String) it.next();
                Object value = properties.get(key);
                if (key.startsWith("audio")) {
                    jsSB.append(key + "=" + value + "\n");
                }
                else if (key.startsWith(audiotype)) {
                    spiSB.append(key + "=" + value + "\n");
                }
                else {
                    extjsSB.append(key + "=" + value + "\n");
                }
            }
            System.out.println(jsSB.toString());
            System.out.println(extjsSB.toString());
            System.out.println(spiSB.toString());
        }
    }

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
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
        if (cdgFileLoaded) {
            tempsMp3 = microseconds / 1000;
        }
        if (seeking) {
            // tempsMp3 = microseconds / 1000;
            String value = "" + properties.get("mp3.position.microseconds");
            System.out.println(("---microseconds--- = " + value));
            tempsMp3 = Long.parseLong(value);
            // System.exit(0);
            tempsMp3 /= 1000;
            System.out.println("tempsMp3 = " + tempsMp3);
            System.out.println("currentRow avant " + current_row);
            current_row = (int) ((tempsMp3 / 3.33) + 0.5);
            seeking = false;
            System.out.println("-----");
            System.out.println("Current row recalculé");
            System.out.println("currentRow après " + current_row);
            System.out.println("-----");

        }
    }

    /**
     * Notification callback of javazoom.jlgui.player.test state.
     * 
     * @param event
     */
    public void stateUpdated(BasicPlayerEvent event) {
        System.out.println("RECU BASICPLAYEREVBNT = " + event.getCode());
        if (cdgFileLoaded) {
            if (event.getCode() == BasicPlayerEvent.PLAYING) {
                System.out.println("RECU BASICPLAYEREVBNT = PLAYING");
                if (!seeking) {
                    System.out.println("NOT SEEKING !");
                    startTimedPlay();
                }
            }
            else {
                if (event.getCode() == BasicPlayerEvent.STOPPED) {
                    System.out.println("RECU BASICPLAYEREVBNT STOPPED");
                    if (!seeking) {
                        System.out.println("ON ARRETE le cdg ! stopCdgOnly()");
                        stopCdgOnly();
                    }
                }
                else {
                    if (event.getCode() == BasicPlayerEvent.PAUSED) {
                        System.out.println("RECU BASICPLAYEREVBNT PAUSED");
                        pause();
                    }
                    else if (event.getCode() == BasicPlayerEvent.RESUMED) {
                        System.out.println("RECU BASICPLAYEREVBNT RESUMED");
                        resume();
                    }
                    else if (event.getCode() == BasicPlayerEvent.SEEKED) {
                        System.out.println("RECU BASICPLAYEREVBNT SEEKED");
                        seeking = true;
                    }
                    else if (event.getCode() == BasicPlayerEvent.SEEKING) {
                        System.out.println("RECU BASICPLAYEREVBNT SEEKING");
                        seeking = true;
                    }
                }
            }

        }
    }

    // ----------------------------------------------
    // End of Méthods required by the pluglin implementation
    // ----------------------------------------------

    private void jbInit() throws Exception {
        jMenuItemLyricsSyncOptions.setText("Lyrics sync options");
        jMenuItemHelp.setText("Help");
        jMenuItemFullScreensOptions.setText("Choose fullscreen resolution");
        jMenuItemFullScreensOptions.addActionListener(new BasicPlayerWindow_jMenuItemFullScreensOptions_actionAdapter(
        this));
        jMenuItemLyricsSyncOptions.setText("Lyrics not in sinc ?");
        jMenuItemLyricsSyncOptions.addActionListener(new BasicPlayerWindow_jMenuItemLyricsSyncOptions_actionAdapter(
        this));
        jMenuItemFullScreenWindow.setText("Fullscreen/Window");
        jMenuItemFullScreenWindow
        .addActionListener(new BasicPlayerWindow_jMenuItemFullScreenWindow_actionAdapter(this));
        jCheckBoxMenuItem2.setText("Use hw full screen mode (win+linux only)");
        jCheckBoxMenuItem2.addActionListener(new BasicPlayerWindow_jCheckBoxMenuItem2_actionAdapter(this));
        jPopupMenu1.add(jMenuItemLyricsSyncOptions);
        jPopupMenu1.addSeparator();
        jPopupMenu1.add(jCheckBoxMenuItem2);
        jPopupMenu1.add(jMenuItemFullScreensOptions);
        jPopupMenu1.add(jMenuItemFullScreenWindow);
        jPopupMenu1.addSeparator();
        jPopupMenu1.add(jMenuItemHelp);
    }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                jPopupMenu1.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    void jMenuItemFullScreensOptions_actionPerformed(ActionEvent e) {
        goToWindowedMode();
        // chooseFullScreenDialog.setVisible(true);
        chooseFullScreenDialog.setVisible(true);
        userDisplayMode = chooseFullScreenDialog.getSelectedDisplayMode();

        // Save in the preferences
        preferences.setProperty("fsWidth", "" + userDisplayMode.getWidth());
        preferences.setProperty("fsHeight", "" + userDisplayMode.getHeight());
        preferences.setProperty("fsDepth", "" + userDisplayMode.getBitDepth());
        preferences.setProperty("fsFreq", "" + userDisplayMode.getRefreshRate());

        panelLyrics.redrawFullImage();
    }

    private void pause() {
        pausedPlay = true;
    }

    private void resume() {
        pausedPlay = false;
    }

    void jMenuItemLyricsSyncOptions_actionPerformed(ActionEvent e) {
        // pops up the cdg options....
        goToWindowedMode();
        cdgOptionsDialog.setVisible(true);
        int nbCdgInstructions = cdgOptionsDialog.getCdgBufferSize();
        preferences.setProperty("nbCdgInstructions", "" + nbCdgInstructions);
        setNbCdgInstructions(nbCdgInstructions);

        panelLyrics.setForceDrawFullImage(cdgOptionsDialog.getRedrawFullImage());
        System.out.println("setting nbCdgInstruction to : " + nbCdgInstructions
        + ". Will take effect on next song played.");

        panelLyrics.redrawFullImage();
        preferences.setProperty("redrawAllFrame", "" + "" + cdgOptionsDialog.getRedrawFullImage());
    }

    /**
     * getPlugin
     * 
     * @return BasicPlayerListener
     */
    public BasicPlayerListener getPlugin() {
        return this;
    }

    void jMenuItemFullScreenWindow_actionPerformed(ActionEvent e) {
        switchFullScreenWindowedMode();
    }

    void jCheckBoxMenuItem2_actionPerformed(ActionEvent e) {
        // Support for hardware full screen mode
        hardwareAcceleratedFullScreenModeSupported = !hardwareAcceleratedFullScreenModeSupported;
        setFullScreenModeOption();

    }

    private void setFullScreenModeOption() {
        preferences.setProperty("hardwareAcceleratedFullScreenModeSupported", ""
        + hardwareAcceleratedFullScreenModeSupported);
        jCheckBoxMenuItem2.setSelected(hardwareAcceleratedFullScreenModeSupported);
        jMenuItemFullScreensOptions.setEnabled(hardwareAcceleratedFullScreenModeSupported);
        jMenuItemFullScreenWindow.setEnabled(hardwareAcceleratedFullScreenModeSupported);

    }
}

class BasicPlayerWindow_jMenuItemFullScreensOptions_actionAdapter implements java.awt.event.ActionListener {
    BasicPlayerWindow adaptee;

    BasicPlayerWindow_jMenuItemFullScreensOptions_actionAdapter(BasicPlayerWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemFullScreensOptions_actionPerformed(e);
    }
}

class BasicPlayerWindow_jMenuItemLyricsSyncOptions_actionAdapter implements java.awt.event.ActionListener {
    BasicPlayerWindow adaptee;

    BasicPlayerWindow_jMenuItemLyricsSyncOptions_actionAdapter(BasicPlayerWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemLyricsSyncOptions_actionPerformed(e);
    }

}

class BasicPlayerWindow_jMenuItemFullScreenWindow_actionAdapter implements java.awt.event.ActionListener {
    BasicPlayerWindow adaptee;

    BasicPlayerWindow_jMenuItemFullScreenWindow_actionAdapter(BasicPlayerWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemFullScreenWindow_actionPerformed(e);
    }
}

class BasicPlayerWindow_jCheckBoxMenuItem2_actionAdapter implements java.awt.event.ActionListener {
    BasicPlayerWindow adaptee;

    BasicPlayerWindow_jCheckBoxMenuItem2_actionAdapter(BasicPlayerWindow adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jCheckBoxMenuItem2_actionPerformed(e);
    }
}
