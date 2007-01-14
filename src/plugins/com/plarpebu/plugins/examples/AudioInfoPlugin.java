package com.plarpebu.plugins.examples;

import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import com.plarpebu.plugins.sdk.FramePlugin;

/**
 * Audio Info
 */
public class AudioInfoPlugin extends FramePlugin implements BasicPlayerListener {
    private JTextField sourceTF = null;

    private JLabel jsInfoLB = null;

    private JScrollPane jsInfoSP = null;

    private JTextArea jsInfoTA = null;

    private JLabel extjsInfoLB = null;

    private JScrollPane extjsInfoSP = null;

    private JTextArea extjsInfoTA = null;

    private JLabel spiInfoLB = null;

    private JScrollPane spiInfoSP = null;

    private JTextArea spiInfoTA = null;

    private JLabel dynspiInfoLB = null;

    private JScrollPane dynspiInfoSP = null;

    private JTextArea dynspiInfoTA = null;

    private Container pane = null;

    private String audiotype = null;

    public AudioInfoPlugin() throws HeadlessException {
        super();
        initUI();
    }

    public void initUI() {
        sourceTF = new JTextField();
        sourceTF.setBounds(new Rectangle(10, 10, 275, 20));
        jsInfoLB = new JLabel("JavaSound info : ");
        jsInfoLB.setBounds(new Rectangle(10, 30, 275, 20));
        jsInfoSP = new JScrollPane();
        jsInfoTA = new JTextArea();
        jsInfoSP.getViewport().add(jsInfoTA);
        jsInfoSP.setBounds(new Rectangle(10, 50, 275, 60));
        extjsInfoLB = new JLabel("Extended JavaSound Info :");
        extjsInfoLB.setBounds(new Rectangle(10, 120, 275, 20));
        extjsInfoSP = new JScrollPane();
        extjsInfoTA = new JTextArea();
        extjsInfoSP.getViewport().add(extjsInfoTA);
        extjsInfoSP.setBounds(new Rectangle(10, 140, 275, 60));
        spiInfoLB = new JLabel("Static SPI Info");
        spiInfoLB.setBounds(new Rectangle(10, 210, 275, 20));
        spiInfoSP = new JScrollPane();
        spiInfoTA = new JTextArea();
        spiInfoSP.getViewport().add(spiInfoTA);
        spiInfoSP.setBounds(new Rectangle(10, 230, 275, 60));
        dynspiInfoLB = new JLabel("Dynamic SPI Info");
        dynspiInfoLB.setBounds(new Rectangle(10, 300, 275, 20));
        dynspiInfoSP = new JScrollPane();
        dynspiInfoTA = new JTextArea();
        dynspiInfoSP.getViewport().add(dynspiInfoTA);
        dynspiInfoSP.setBounds(new Rectangle(10, 320, 275, 90));

        pane = this.getContentPane();
        pane.setLayout(null);
        pane.add(sourceTF);
        pane.add(jsInfoLB);
        pane.add(jsInfoSP);
        pane.add(extjsInfoLB);
        pane.add(extjsInfoSP);
        pane.add(spiInfoLB);
        pane.add(spiInfoSP);
        pane.add(dynspiInfoLB);
        pane.add(dynspiInfoSP);

        this.setSize(295, 445);
        this.setTitle("Audio Info " + getVersion());

        readPreferences();
    }

    public void opened(Object stream, Map properties) {

        if (stream != null) {
            if (stream instanceof File) {
                sourceTF.setText(((File) stream).getAbsolutePath());
            }
            else if (stream instanceof URL) {
                sourceTF.setText(((URL) stream).toString());
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
            System.out.println("key = " + key);
            System.out.println("value = " + value);
            System.out.println("audiotype = " + audiotype);
            if (key.startsWith("audio")) {
                jsSB.append(key + "=" + value + "\n");
            }
            else if ((audiotype != null) && (key.startsWith(audiotype))) {
                spiSB.append(key + "=" + value + "\n");
            }
            else {
                extjsSB.append(key + "=" + value + "\n");
            }
        }
        jsInfoTA.setText(jsSB.toString());
        extjsInfoTA.setText(extjsSB.toString());
        spiInfoTA.setText(spiSB.toString());
    }

    public void stateUpdated(BasicPlayerEvent event) {}

    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
        if (!isVisible()) return;

        Iterator it = properties.keySet().iterator();
        StringBuffer dynspiSB = new StringBuffer();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = properties.get(key);
            if ((audiotype != null) && (key.startsWith(audiotype))) {
                dynspiSB.append(key + "=" + value + "\n");
            }
        }
        dynspiInfoTA.setText(dynspiSB.toString());
    }

    public String getName() {
        return "AudioInfo";
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
    }

    public String getVersion() {
        return "v1.0";
    }

}