package plugins.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import pluginsSDK.PanelPlugin;
import test.LabelClock;

/**
 * Info plugin
 */
public class InfoPlugin extends PanelPlugin implements BasicPlayerListener, Runnable {
    private JTextField sourceTF = null;

    private String audiotype = null;

    // mes infos perso
    // private SevenSegClockPanel time = null;
    private LabelClock time;

    private JLabel title = null;

    private DefilPanel dp = null;

    private int speed = 100; // temps de pause

    private BasicController controller = null;

    static Border DefaultBorder;

    private int byteslength = -1;

    private int milliseconds = -1;

    static {
        CompoundBorder compoundborder = new CompoundBorder(new LineBorder(Color.lightGray, 2), new BevelBorder(1,
        Color.white, Color.darkGray));
        DefaultBorder = new CompoundBorder(new BevelBorder(0, Color.white, Color.darkGray), compoundborder);
    }

    /**
     * Constructor
     * 
     * @throws HeadlessException
     */
    public InfoPlugin() throws HeadlessException {

        initUI();
    }

    /**
     * Initialize UI
     */
    public void initUI() {

        // time = new SevenSegClockPanel();
        time = new LabelClock();
        title = new JLabel(" ");
        title.setFont(new Font("BankGothic Md BT", 0, 16));

        Box b2 = Box.createVerticalBox();
        b2.setAlignmentX(LEFT_ALIGNMENT);
        dp = new DefilPanel(" ");
        b2.add(dp);
        b2.add(Box.createRigidArea(new Dimension(180, 0)));
        b2.validate();

        Box b3 = Box.createHorizontalBox();
        b3.setAlignmentX(CENTER_ALIGNMENT);
        JPanel pp = new JPanel();
        pp.add(time);

        b3.add(Box.createRigidArea(new Dimension(5, 1)));
        b3.add(pp);
        b3.add(Box.createRigidArea(new Dimension(25, 15)));
        b3.add(b2);
        b3.validate();

        this.add(b3);
    }

    public void opened(Object stream, Map properties) {
        if (properties.containsKey("audio.length.bytes")) {
            byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
        }

        if (properties.containsKey("duration")) {
            milliseconds = (int) (((Long) properties.get("duration")).longValue()) / 1000;
        }

        if (stream != null) {
            System.out.println("stream != null");
            if (stream instanceof File) {
                dp.setString(((File) stream).getName());
                System.out.println("dp.setString(" + ((File) stream).getName());
            }
            else if (stream instanceof URL) {
                sourceTF.setText(((URL) stream).toString());
            }
        }
    }

    public void stateUpdated(BasicPlayerEvent event) {}

    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {

        float progress = bytesread * 1.0f / this.byteslength * 1.0f;
        long progressMilliseconds = (long) (progress * milliseconds);
        // time.setTimeToDisplay(millisec_to_time(progressMilliseconds));
        time.setText(millisec_to_time(progressMilliseconds));

        Iterator it = properties.keySet().iterator();
        StringBuffer dynspiSB = new StringBuffer();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = properties.get(key);
            if ((audiotype != null) && (key.startsWith(audiotype))) {
                dynspiSB.append(key + "=" + value + "\n");
            }
        }
    }

    public String getName() {
        return "Info";
    }

    /**
     * run
     */
    public void run() {

        while (true) {
            dp.setMsgPosX((dp.getMsgPosX()) - 2);
            if (dp.getMsgPosX() <= -(dp.getMsgLength())) dp.setMsgPosX(dp.getSize().width);
            dp.repaint();

            try {
                Thread.sleep(speed);
            }
            catch (InterruptedException e) {}
        }
    }

    private String millisec_to_time(long time_ms) {
        int seconds = (int) Math.floor(time_ms / 1000);
        int minutes = (int) Math.floor(seconds / 60);
        int hours = (int) Math.floor(minutes / 60);
        minutes = minutes - hours * 60;
        seconds = seconds - minutes * 60 - hours * 3600;
        String strhours = "" + hours;
        String strminutes = "" + minutes;
        String strseconds = "" + seconds;
        if (strseconds.length() == 1) {
            strseconds = "0" + strseconds;
        }
        if (strminutes.length() == 1) {
            strminutes = "0" + strminutes;
        }
        if (strhours.length() == 1) {
            strhours = "0" + strhours;
        }
        return (strhours + ":" + strminutes + ":" + strseconds);
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
     * setController
     * 
     * @param controller
     *        BasicController
     */
    public void setController(BasicController controller) {
        this.controller = controller;
    }

    public String getVersion() {
        return "v1.0";
    }

}
