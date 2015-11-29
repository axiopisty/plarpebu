package com.plarpebu.plugins.basic.pan_gain;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import com.plarpebu.plugins.sdk.PanelPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pan Gain Plugin
 */
public class PanGainPlugin extends PanelPlugin implements BasicPlayerListener, MouseListener, MouseMotionListener {

  private final static Logger logger = LoggerFactory.getLogger(PanGainPlugin.class);

  public static final String NAME = "Pan Gain";

  private JSlider panslider = null;

  private JSlider gainslider = null;

  private DefaultBoundedRangeModel panmodel = null;

  private DefaultBoundedRangeModel gainmodel = null;

  private int scale = 100;

  private BasicController controller = null;

  /**
   * Constructor
   *
   * @throws HeadlessException
   */
  public PanGainPlugin() throws HeadlessException {
    super();
    initUI();
  }

  /**
   * Init UI
   */
  public void initUI() {
    panmodel = new DefaultBoundedRangeModel(0, 1, -scale, scale + 1);
    gainmodel = new DefaultBoundedRangeModel((scale * 80 / 100), 1, 0, scale + 1);
    // panLB = new JLabel("Pan : ");
    // gainLB = new JLabel("Gain : ");
    panslider = new JSlider(panmodel);
    panslider.setPaintTicks(true);
    panslider.setMajorTickSpacing(100);
    panslider.setMinorTickSpacing(5);
    panslider.addMouseListener(this);
    panslider.addMouseMotionListener(this);

    gainslider = new JSlider(gainmodel);
    gainslider.setPaintTicks(true);
    gainslider.setMajorTickSpacing(50);
    gainslider.setMinorTickSpacing(2);
    gainslider.addMouseListener(this);
    gainslider.addMouseMotionListener(this);

    panslider.setMaximumSize(new Dimension(100, 20));
    gainslider.setMaximumSize(new Dimension(120, 20));

    panslider.setEnabled(false);
    gainslider.setEnabled(false);

    // this.setLayout(new GridLayout(0, 2));
    // this.add(gainLB);
    // this.add(panLB);
    Box b = Box.createVerticalBox();
    b.add(gainslider);
    b.add(Box.createRigidArea(new Dimension(20, 20)));
    b.add(panslider);
    this.add(b);
  }

  public String getName() {
    return NAME;
  }

  public void setController(BasicController controller) {
    this.controller = controller;
  }

  public void opened(Object stream, Map properties) {
    panslider.setEnabled(true);
    gainslider.setEnabled(true);
  }

  public void stateUpdated(BasicPlayerEvent event) {
  }

  public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
    logger.debug("mouse released");
    if(panslider.isEnabled() && gainslider.isEnabled()) {
      try {
        if(e.getSource() == panslider) {
          double pan = panmodel.getValue() * 1.0 / scale * 1.0;
          controller.setPan(pan);
          logger.debug("controller .setpan");
        } else if(e.getSource() == gainslider) {
          double gain = gainmodel.getValue() * 1.0 / scale * 1.0;
          controller.setGain(gain);
          logger.debug("controller.setgain");
        }
      } catch(BasicPlayerException ex) {
        logger.warn(ex.getMessage(), ex);
      }
    }
  }

  /**
   * getPlugin
   *
   * @return BasicPlayerListener
   */
  public BasicPlayerListener getPlugin() {
    return this;
  }

  /**
   * mouseDragged
   *
   * @param e MouseEvent
   */
  public void mouseDragged(MouseEvent e) {
    if(panslider.isEnabled() && gainslider.isEnabled()) {

      if(e.getSource() == panslider) {
        int pan = panmodel.getValue();
        if(pan < 0) {
          logger.debug("LEFT: " + -pan + "%");
        } else if(pan > 0) {
          logger.debug("RIGHT : " + pan + "%");
        } else {
          logger.debug("CENTER");
        }
      } else if(e.getSource() == gainslider) {
        int gain = gainmodel.getValue();
        logger.debug("Volume : " + gain + "%");
      }
    }
  }

  /**
   * mouseMoved
   *
   * @param e MouseEvent
   */
  public void mouseMoved(MouseEvent e) {
  }

  public String getVersion() {
    return "v1.0";
  }

}
