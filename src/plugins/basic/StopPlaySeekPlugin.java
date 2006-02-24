package plugins.basic;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import player.test.*;
import plugins.playlist.*;
import pluginsSDK.*;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerException;


/**
 *
 */

public class StopPlaySeekPlugin
    extends PanelPlugin
    implements BasicPlayerListener, ActionListener, MouseListener {

  private JButton play = null;
  private JButton stop = null;
  private JButton pause = null;
  private JButton next = null;
  private JButton prev = null;
  private JButton add = null;

  private JSlider slider = null;
  private JLabel seekLB = null;
  private DefaultBoundedRangeModel model = null;
  private int modelscale = 1000;

  private JPanel panelButton = null;
  private boolean isFirst = true;

  // Audio
  private int byteslength = -1;

  private BasicController controller = null;
  private static boolean paused = false;


  public StopPlaySeekPlugin() throws HeadlessException {
    initUI();
  }

  public void initUI() {


    model = new DefaultBoundedRangeModel(0, 1, 0, modelscale);
    seekLB = new JLabel("Seek : ");
    slider = new JSlider(model);
    slider.setPreferredSize(new Dimension(50, 20));
    slider.setPaintTicks(true);
    slider.setMajorTickSpacing(50);
    slider.setMinorTickSpacing(10);
    slider.addMouseListener(this);

    Box b = Box.createVerticalBox() ;
   // this.setLayout(new GridLayout(2, 0));
    panelButton = new JPanel();

    prev = addButton(panelButton, "prev", "prev", "icones/tmp/bigPrev.gif");
    prev.addActionListener(this);

    play = addButton(panelButton, "play", "play", "icones/tmp/bigPlay.gif");
    play.addActionListener(this);

    pause = addButton(panelButton, "pause", "pause", "icones/tmp/bigPause.gif");
    pause.addActionListener(this);

    stop = addButton(panelButton, "stop", "stop", "icones/tmp/bigStop.gif");
    stop.addActionListener(this);

    next = addButton(panelButton, "next", "next", "icones/tmp/bigNext.gif");
    next.addActionListener(this);

    add = addButton(panelButton, "add", "add file", "icones/tmp/bigEject.gif");
    add.addActionListener(this);

    slider.setEnabled(false);
    b.add(slider);
    b.add(Box.createRigidArea(new Dimension(1, 3)));
    b.add(panelButton);
    this.add(b);

  }

  public String getName() {
    return "Stop Play Seek Plugin";
  }



  /** Permet de créer un nouveau bouton en lui associant, une action,
          un tooltip et une icone
          @param p composant dans lequel il va etre ajouté
          @param name son nom pour lui associer une action(actionPerformed)
          @param tooltiptext le tooltip à afficher
          @param imageName le chemin d'accés à l'icone */
  public JButton addButton(JComponent p, String name, String tooltiptext,
                    String imageName) {
    JButton b;
    if ( (imageName == null) || (imageName.equals(""))) {
      b = (JButton) p.add(new JButton(name));
    }
    else {
      java.net.URL u = this.getClass().getResource(imageName);
      if (u != null) {
        ImageIcon im = new ImageIcon(u);

        b = (JButton) p.add(new JButton(im));
      }
      else {
        b = (JButton) p.add(new JButton(name));
        //b.setActionCommand(name);
      }
    }

    b.setToolTipText(tooltiptext);
    Insets insets = new Insets(0, 0, 0, 0);
    b.setMargin(insets);

    return b;
  }

  public void actionPerformed(ActionEvent e) {
    try {
      if (e.getSource() == play) {
        if (Player.isActivate() && isFirst){
          File f = PlayListPlugin.getFileSelected();
          controller.open(f);
          isFirst = false;
          PlayListPlugin.setPlaying(true);
        }
        controller.play();
        slider.setEnabled(true);
      }
      else if (e.getSource() == stop) {
        controller.stop();
        model.setValue(0);
        slider.setEnabled(false);
        PlayListPlugin.setPlaying(false);
      }
      else if (e.getSource() == next) {
        controller.stop();
        model.setValue(0);
        PlayListPlugin.getNext();

      }
      else if (e.getSource() == prev) {
        controller.stop();
        model.setValue(0);
        PlayListPlugin.getPrev();
      }

      else if (e.getSource() == pause) {
        if (paused == true) {
          controller.resume();
          paused = false;
        }
        else {
          controller.pause();
          paused = true;
        }
      }
      else if (e.getSource() == add){
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);
        fc.setDialogTitle("Add a file");
        fc.showOpenDialog(this);
        File file = fc.getSelectedFile();
        if(file != null) {
          controller.open(file);
          //if (Player.isActivate()) {
            PlayListPlugin.addElementToPlayList(file.getAbsolutePath());
          //}
        }
      }

    }
    catch (BasicPlayerException e1) {
      e1.printStackTrace();
    }
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
    if (e.getSource() == slider) {
      try {
        long skp = -1;
        //synchronized (model) {
          skp = (long) ( (model.getValue() * 1.0f / modelscale * 1.0f) *
                        byteslength);
        //}
        controller.seek(skp);
      }
      catch (BasicPlayerException e1) {
        e1.printStackTrace();
      }
    }
  }

  public void setController(BasicController controller) {
    this.controller = controller;
  }

  public void opened(Object stream, Map properties) {
    slider.setEnabled(true);
    if (properties.containsKey("audio.length.bytes")) {
      byteslength = ( (Integer) properties.get("audio.length.bytes")).intValue();
    }
  }

  public void stateUpdated(BasicPlayerEvent event) {
    System.out.println("Player Event : " + event);
  }

  public void progress(int bytesread, long microseconds, byte[] pcmdata,
                       Map properties) {

    float progress = bytesread * 1.0f / this.byteslength * 1.0f;
   model.setValue( (int) (progress * modelscale));
  }


  /**
   * getPlugin
   *
   * @return playerPlugin
   */
  public BasicPlayerListener getPlugin() {
    return this;
  }

  public String getVersion() {
    return "1.0";
  }


}
