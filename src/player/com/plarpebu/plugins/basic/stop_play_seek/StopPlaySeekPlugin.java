package com.plarpebu.plugins.basic.stop_play_seek;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.plarpebu.Player;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicController;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicPlayerEvent;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicPlayerException;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicPlayerListener;
import com.plarpebu.plugins.basic.SwingUtils;
import com.plarpebu.plugins.basic.playlist.PlayListPlugin;
import com.plarpebu.plugins.sdk.PanelPlugin;

/**
 *
 */
public class StopPlaySeekPlugin extends PanelPlugin implements BasicPlayerListener, ActionListener, MouseListener {

    private JButton play = null;

    private JButton stop = null;

    private JButton pause = null;

    private JButton next = null;

    private JButton prev = null;

    private JButton add = null;

    private JSlider slider = null;

    // private JLabel seekLB = null;

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
        // seekLB = new JLabel("Seek : ");
        slider = new JSlider(model);
        slider.setPreferredSize(new Dimension(50, 20));
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.addMouseListener(this);

        Box b = Box.createVerticalBox();
        // this.setLayout(new GridLayout(2, 0));
        panelButton = new JPanel();

        prev = SwingUtils.addButton(panelButton, "prev", "prev", "/icons/big/bigPrev.gif");
        prev.addActionListener(this);

        play = SwingUtils.addButton(panelButton, "play", "play", "/icons/big/bigPlay.gif");
        play.addActionListener(this);

        pause = SwingUtils.addButton(panelButton, "pause", "pause", "/icons/big/bigPause.gif");
        pause.addActionListener(this);

        stop = SwingUtils.addButton(panelButton, "stop", "stop", "/icons/big/bigStop.gif");
        stop.addActionListener(this);

        next = SwingUtils.addButton(panelButton, "next", "next", "/icons/big/bigNext.gif");
        next.addActionListener(this);

        add = SwingUtils.addButton(panelButton, "add", "add file", "/icons/big/bigEject.gif");
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

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == play) {
                if (Player.isActivate() && isFirst) {
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
            else if (e.getSource() == add) {
                JFileChooser fc = new JFileChooser();
                fc.setMultiSelectionEnabled(false);
                fc.setDialogTitle("Add a file");
                fc.showOpenDialog(this);
                File file = fc.getSelectedFile();
                if (file != null) {
                    controller.open(file);
                    // if (Player.isActivate()) {
                    PlayListPlugin.addElementToPlayList(file.getAbsolutePath());
                    // }
                }
            }

        }
        catch (BasicPlayerException e1) {
            e1.printStackTrace();
        }
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == slider) {
            try {
                long skp = -1;
                // synchronized (model) {
                skp = (long) ((model.getValue() * 1.0f / modelscale * 1.0f) * byteslength);
                // }
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
            byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
        }
    }

    public void stateUpdated(BasicPlayerEvent event) {
        System.out.println("Player Event : " + event);
    }

    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {

        float progress = bytesread * 1.0f / this.byteslength * 1.0f;
        model.setValue((int) (progress * modelscale));
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
        return "v1.0";
    }

}
