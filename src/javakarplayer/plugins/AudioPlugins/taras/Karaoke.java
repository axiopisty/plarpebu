package javakarplayer.plugins.AudioPlugins.taras;

/*
 * Karaoke player in Java
 *
 * Created on 3 July 2002
 */
import java.util.Vector;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import java.awt.*;
import javax.swing.*;

import java.awt.event.*;
import com.l2fprod.common.swing.JFontChooser;
import pluginsSDK.JFrameWithPreferences;
import player.test.utils.ExitListenerSecurityManager;
import fr.unice.buffa.*;
import pluginsSDK.FramePluginWithFullScreenSupport;
import javax.swing.event.*;
import pluginsSDK.Iconifiable;



/**
 * MIDI Karaoke player
 *
 * @author  Taras P. Galchenko
 */
public class Karaoke extends FramePluginWithFullScreenSupport implements MetaEventListener {

  private KaraokePane karaokePane;
  private Vector song = null;
  private Vector frames = null;
  private KaraokeProperties props = null;
  private Sequencer sequencer = null;
  private boolean paused = false;

  private PreferencesDialog preferencesDialog = new PreferencesDialog();

  BorderLayout borderLayout1 = new BorderLayout();
  JPanel toolbar = new JPanel();
  JScrollBar tempoSlider = new JScrollBar();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JPanel jPanel1 = new JPanel();
  JButton jButtonPreferences = new JButton();

  private String artist;
  private String songTitle;
  JPopupMenu jPopupMenu1 = new JPopupMenu();
  JMenuItem jMenuItem1 = new JMenuItem();
  JCheckBoxMenuItem jCheckBoxMenuItem1 = new JCheckBoxMenuItem();
  JMenuItem jMenuItem3 = new JMenuItem();

  private Iconifiable playerUI;

  public Karaoke(Sequencer sequencer) {
    //setUndecorated(true);
    //getRootPane().setWindowDecorationStyle(JRootPane.NONE);

    this.sequencer = sequencer;
    sequencer.addMetaEventListener(this);
    props = new KaraokeProperties();
    karaokePane = new KaraokePane(props);

    try {
      jbInit();
      // Add karaoke pane
      getContentPane().add(karaokePane, BorderLayout.CENTER);
//      toolbar.hide();
//      jPanel1.hide();

      //setGlassPane(karaokePane);
      //setSize(400, 300);
      //setUndecorated(true);

      // for exit() notification and automatic preferences saving behavior
      ExitListenerSecurityManager sm = (ExitListenerSecurityManager) System.getSecurityManager();
      sm.addSystemExitListener(this);

      MouseListener popupListener = new PopupListener();
      addMouseListener(popupListener);

      loadPreferences();

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setPlayerUI(Iconifiable playerUI) {
    this.playerUI = playerUI;
  }

  protected void goToFullScreen() {
    toolbar.setVisible(false);
    jPanel1.setVisible(false);

    playerUI.minimize();
    super.goToFullScreen();
    //karaokePane.redrawAll();
  }

  protected void goToWindowedMode() {
    toolbar.setVisible(true);
    jPanel1.setVisible(true);

    playerUI.setToOriginalSize();
    super.goToWindowedMode();
    //karaokePane.redrawAll();
  }

  class PopupListener
      extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        jPopupMenu1.show(e.getComponent(),
                         e.getX(), e.getY());
      }
    }
  }

  private void loadPreferences() {
    //Read all preferences
    String stringValue = null;
    Boolean value = null;

    // Préférences, inherited from JFrameWithPreferences
    setPreferencesFileNames("preferences", "KarMidiLyrics.properties",
                            "defaultKarMidiLyrics.properties");
    readPreferences();

    // width and height
    props.width = Integer.parseInt(preferences.getProperty("width"));
    props.height = Integer.parseInt(preferences.getProperty("height"));

    // Look for font
    props.fontFace = preferences.getProperty("font");
    props.style = Integer.parseInt(preferences.getProperty("fontStyle"));

    // Antialiasing
    // Outlined text
    if ( (stringValue = preferences.getProperty("AntialiasedText")) != null) {
      value = Boolean.valueOf(stringValue);
      props.antiAliasedText = value.booleanValue();
      jCheckBoxMenuItem1.setSelected(props.antiAliasedText);
    }

    // Look for text colors
    props.syllabeToSingColor = StringToColor(preferences.getProperty("SyllabeToSingColor"));
    props.sungSyllabesColor = StringToColor(preferences.getProperty("SungSyllabesColor"));
    props.syllabesNotSungYetColor = StringToColor(preferences.getProperty("SyllabesNotSungYetColor"));

    // Shadowed text
    if ( (stringValue = preferences.getProperty("DisplayShadowedText")) != null) {
      value = Boolean.valueOf(stringValue);
      props.displayShadow = value.booleanValue();
    }
    props.shadowColor = StringToColor(preferences.getProperty("ShadowColor"));

    // Outlined text
    if ( (stringValue = preferences.getProperty("DisplayOutlinedText")) != null) {
      value = Boolean.valueOf(stringValue);
      props.displayOutline = value.booleanValue();
    }
    props.outlineColor = StringToColor(preferences.getProperty("OutlineColor"));
    props.outlineWidth = Integer.parseInt(preferences.getProperty("OutlineWidth"));

    // Charsets
    if ( (stringValue = preferences.getProperty("AutodetectCharset")) != null) {
      value = Boolean.valueOf(stringValue);
      props.autodetectCharset = value.booleanValue();
    }
    props.charset = preferences.getProperty("Charset");
    props.charsetHint = Integer.parseInt(preferences.getProperty("CharsetHint"));


    // Background mode
    props.backgroundType = Integer.parseInt(preferences.getProperty("BackgroundMode"));

    // Monochrome background
    props.bgColor = StringToColor(preferences.getProperty("MonoChromeBackgroundColor"));

    // Image background
    props.bgImageFilename = preferences.getProperty("BackgroundImage");

    try {
      props.bgImage = Toolkit.getDefaultToolkit().createImage(props.
          bgImageFilename);
      MediaTracker mt = new MediaTracker(this);
      mt.addImage(props.bgImage, 0);
      while(!mt.checkAll(true))
        try
        {
          Thread.sleep(20L);
        }
        catch(Exception exception) { }
    }
    catch (Exception ex) {
      System.out.println("Background image : " + props.bgImageFilename + " does not exists");
    }

    // Background gradiant colors
    props.startGradiantColor = StringToColor(preferences.getProperty("GradiantColorStart"));
    props.endGradiantColor = StringToColor(preferences.getProperty("GradiantColorEnd"));

    // Ribbon
    props.ribbonColor = StringToColor(preferences.getProperty("RibbonColor"));
    props.ribbonWidth = Integer.parseInt(preferences.getProperty("RibbonWidth"));

    // Lyrics layout
    props.lines = Integer.parseInt(preferences.getProperty("NbLyricsLinesDisplayed"));
    props.readLine = Integer.parseInt(preferences.getProperty("NbSungLyricsLinesDisplayed"));
    props.cols = Integer.parseInt(preferences.getProperty("NbColumns"));
  }

  /** We redefine the inherited method so that visibility is always set to false */
  public void exiting() {
    setVisible(false);

     copyPropsIntoPreferences();

    super.exiting();
  }

  private void copyPropsIntoPreferences() {

    // Width and height
    preferences.setProperty("width", "" + props.width);
    preferences.setProperty("height", "" + props.height);

    // font
    preferences.setProperty("font", props.fontFace);
    preferences.setProperty("fontStyle", "" + props.style);

    // Antialiasing
    preferences.setProperty("AntialiasedText", "" + props.antiAliasedText);

    // text colors
    preferences.setProperty("SyllabeToSingColor", colorToString(props.syllabeToSingColor));
    preferences.setProperty("SungSyllabesColor", colorToString(props.sungSyllabesColor));
    preferences.setProperty("SyllabesNotSungYetColor", colorToString(props.syllabesNotSungYetColor));

    // shadowedText
    preferences.setProperty("DisplayShadowedText", "" + props.displayShadow);
    preferences.setProperty("ShadowColor", colorToString(props.shadowColor));

    // outlined text
    preferences.setProperty("DisplayOutlinedText", "" + props.displayOutline);
    preferences.setProperty("OutlineColor", colorToString(props.outlineColor));
    preferences.setProperty("OutlineWidth", "" + props.outlineWidth);

    // charsets
    preferences.setProperty("AutodetectCharset", "" + props.autodetectCharset);
    preferences.setProperty("Charset", props.charset);
    preferences.setProperty("CharsetHint", "" + props.charsetHint);
    // background mode
    preferences.setProperty("BackgroundMode", "" + props.backgroundType);

    // Monochrome background color
    preferences.setProperty("MonoChromeBackgroundColor", colorToString(props.bgColor));

    // Image background
    preferences.setProperty("BackgroundImage", props.bgImageFilename);

    // Gradiant background
     preferences.setProperty("GradiantColorStart", colorToString(props.startGradiantColor));
     preferences.setProperty("GradiantColorEnd", colorToString(props.endGradiantColor));

     // Ribbon
     preferences.setProperty("RibbonColor", colorToString(props.ribbonColor));
     preferences.setProperty("RibbonWidth", "" + props.ribbonWidth);

     // Lyrics Layout
     preferences.setProperty("NbLyricsLinesDisplayed", "" + props.lines);
     preferences.setProperty("NbSungLyricsLinesDisplayed", "" + props.readLine);
     preferences.setProperty("NbColumns", "" + props.cols);
  }

  public void setPreferencePaneAccordingToProps() {
    preferencesDialog.init(props);
  }

  // The window will appear centered
  public void setVisibleOld(boolean flag) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    setLocation( (screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
    super.setVisible(flag);
  }


  /**
   * Positioning in the sequence
   *
   * @param pos       new position in ticks
   */
  public void seek(long pos) {
    karaokePane.seek(pos, true);
  }

  /**
   * Loads new sequence, formats the text, sets text to the karaoke
   * pane if seq != null and closes song if seq = null. Returns true if we have a karaoke
   * file (.kar or midi with text events)
   *
   * @param seq       sequence to process
   */
  public boolean setSong(Sequence seq) throws Exception {
    Lyrics.setCurrentCharset(props.charset);
    song = Lyrics.read(seq);

    System.out.println("Nb frames dans le morceau : " + song.size());
    if(song.size() == 0) return false;

    Lyrics.preformat(song, props.cols);
    artist = Lyrics.getArtist();
    songTitle = Lyrics.getTitle();
    System.out.println("***SET SONG*** : " + artist + " " + songTitle);

    frames = Lyrics.format(song, props.readLine, props.lines,
                           seq.getTickLength());
    karaokePane.setSong(song, frames, props);
    tempoSlider.setEnabled(seq != null);
    // make sure visualKaraoke Frame is visible
    setVisible(true);
    return true;
  }

  /**
   * Callback method for meta event listener. Sifts text events
   * and sends them to karaoke pane
   *
   * @param mm    MIDI meta message
   */
  public void meta(MetaMessage mm) {
    byte[] data = mm.getData();
    if (data.length > 0 && data[0] == '@')
      return;

    if (mm.getType() == Syllable.ST_TEXT
        || mm.getType() == Syllable.ST_LYRICS) {
      karaokePane.seek(sequencer.getTickPosition(), false);
    }
  }

  /**
   * Class that promotes pulsation ribbon every 25 milliseconds
   *
   * When playback is stopped, method stopIt is called to redraw
   * control buttons.
   */

  public void pulse(long pos) {
    karaokePane.pulse(pos);
  }

  /**
   * Sets tempo. x should be x greater than zero
   *
   * @param x         tempo factor
   */
  void setTempo(float x) {
    if (sequencer != null)
      sequencer.setTempoFactor(x);
  }

  public Karaoke() {
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    this.getContentPane().setBackground(UIManager.getColor("Tree.selectionBorderColor"));
    this.setTitle("Kar midi lyrics player V0.99");
    tempoSlider.setMaximum(10);
    tempoSlider.setMinimum(1);
    tempoSlider.setOrientation(JScrollBar.HORIZONTAL);
    //tempoSlider.setValue(5);
    tempoSlider.setValues(5, 1, 0, 10);
    tempoSlider.addAdjustmentListener(new Karaoke_tempoSlider_adjustmentAdapter(this));
    jLabel1.setText("Slower");
    jLabel2.setText("Faster");
    toolbar.setLayout(gridBagLayout1);
    toolbar.setBackground(UIManager.getColor("textInactiveText"));
    jButtonPreferences.setText("Preferences");
    jButtonPreferences.addActionListener(new Karaoke_jButtonPreferences_actionAdapter(this));
    jMenuItem1.setText("Preferences");
    jMenuItem1.addActionListener(new Karaoke_jMenuItem1_actionAdapter(this));
    jCheckBoxMenuItem1.setText("Antialiasing");
    jCheckBoxMenuItem1.addActionListener(new Karaoke_jCheckBoxMenuItem1_actionAdapter(this));
    jMenuItem3.setText("Sho/Hide toolbar");
    jMenuItem3.addActionListener(new Karaoke_jMenuItem3_actionAdapter(this));
    this.getContentPane().add(toolbar, BorderLayout.NORTH);
    toolbar.add(jLabel2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 0, 0, 10), 0, 0));
    toolbar.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 8, 0, 0), 0, 0));
    toolbar.add(tempoSlider, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(2, 0, 0, 0), 258, -4));
    this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(jButtonPreferences, null);
    jPopupMenu1.add(jMenuItem1);
    jPopupMenu1.add(jCheckBoxMenuItem1);
    jPopupMenu1.add(jMenuItem3);
  }


  void tempoSlider_adjustmentValueChanged(AdjustmentEvent e) {
    setTempo( (float) e.getValue() / 5);
  }


  void jButtonPreferences_actionPerformed(ActionEvent e) {
    showPreferencesDialog();
  }

  private void showPreferencesDialog() {
    preferencesDialog.setKaraokePane(karaokePane);
    preferencesDialog.setVisible(true);


    // as the dialog is modal, get the charset autodetection mode and charset value
    Lyrics.setCharsetAutodetection(preferencesDialog.getCharsetAutodetection());
    props.autodetectCharset=preferencesDialog.getCharsetAutodetection();

    if(!preferencesDialog.getCharsetAutodetection()) {
      // If we are not in autodetection mode
      Lyrics.setCurrentCharset(preferencesDialog.getCharset());
      props.charset=preferencesDialog.getCharset();
    } else {
      Lyrics.setCharsetHint(props.charsetHint);
    }
  }

  public String getSongTitle() {
    return songTitle;
  }
  public String getArtist() {
    return artist;
  }

  void jMenuItem1_actionPerformed(ActionEvent e) {
    showPreferencesDialog();
  }

  void jCheckBoxMenuItem1_actionPerformed(ActionEvent e) {
    karaokePane.setAntialiasing(jCheckBoxMenuItem1.isSelected());
  }

  void jMenuItem3_actionPerformed(ActionEvent e) {
    toolbar.setVisible(!toolbar.isVisible());
  }
}

class Karaoke_tempoSlider_adjustmentAdapter
    implements java.awt.event.AdjustmentListener {
  Karaoke adaptee;

  Karaoke_tempoSlider_adjustmentAdapter(Karaoke adaptee) {
    this.adaptee = adaptee;
  }

  public void adjustmentValueChanged(AdjustmentEvent e) {
    adaptee.tempoSlider_adjustmentValueChanged(e);
  }
}

class Karaoke_jButtonPreferences_actionAdapter implements java.awt.event.ActionListener {
  Karaoke adaptee;

  Karaoke_jButtonPreferences_actionAdapter(Karaoke adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonPreferences_actionPerformed(e);
  }
}

class Karaoke_jMenuItem1_actionAdapter implements java.awt.event.ActionListener {
  Karaoke adaptee;

  Karaoke_jMenuItem1_actionAdapter(Karaoke adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jMenuItem1_actionPerformed(e);
  }
}

class Karaoke_jCheckBoxMenuItem1_actionAdapter implements java.awt.event.ActionListener {
  Karaoke adaptee;

  Karaoke_jCheckBoxMenuItem1_actionAdapter(Karaoke adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jCheckBoxMenuItem1_actionPerformed(e);
  }
}

class Karaoke_jMenuItem3_actionAdapter implements java.awt.event.ActionListener {
  Karaoke adaptee;

  Karaoke_jMenuItem3_actionAdapter(Karaoke adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jMenuItem3_actionPerformed(e);
  }
}