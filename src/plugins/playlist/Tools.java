package plugins.playlist;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javakarplayer.plugins.AudioPlugins.taras.FontDialogChoser;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Playlist Configuration UI
 */
public class Tools extends JFrame implements ActionListener, ItemListener, PropertyChangeListener {

    private Container pane = null;

    private JPanel fontPanel;

    private JPanel cbPanel;

    private JPanel itemPanel;

    private JPanel itemPanel2;

    private JLabel fgILabel;

    private JLabel bgILabel;

    private JLabel fgSLabel;

    private JLabel bgSLabel;

    private JCheckBox cb1;

    private JCheckBox cb2;

    private JCheckBox cb3;

    private ColorControl fgI = null;

    private ColorControl bgI = null;

    private ColorControl fgS = null;

    private ColorControl bgS = null;

    private boolean change = false;

    private PlayListPlugin playlist = null;

    private Color bgColor;

    private Color fgColor;

    private Color selectionFgColor;

    private Color selectionBgColor;

    private FontDialogChoser fc = new FontDialogChoser();

    private JButton chooseFontButton;

    /**
     * Constructor
     * 
     * @param pl
     */
    public Tools(PlayListPlugin pl) {
        super("PlayList configuration");
        playlist = pl;
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set visible. The window will appear centered.
     */
    public void setVisible(boolean flag) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        super.setVisible(flag);
    }

    /**
     * Initialize
     * 
     * @throws Exception
     */
    void jbInit() throws Exception {

        pane = getContentPane();
        pane.setLayout(new GridLayout(4, 0));

        fontPanel = new JPanel(new GridLayout(3, 2));
        fontPanel.setBorder(BorderFactory.createTitledBorder("Font"));

        chooseFontButton = new JButton("Choose Font");
        chooseFontButton.addActionListener(this);

        fontPanel.add(chooseFontButton);

        cb1 = new JCheckBox("Include SubFolders for Drag and Drop", true);

        cb2 = new JCheckBox("Show Player Buttons in StatusBar", true);
        cb2.addItemListener(this);

        cb3 = new JCheckBox("Single Song Mode", false);
        cb3.addItemListener(this);

        cbPanel = new JPanel(new GridLayout(4, 0));
        cbPanel.add(cb1);
        cbPanel.add(cb2);
        cbPanel.add(cb3);

        itemPanel = new JPanel(new GridLayout(2, 2));
        itemPanel.setBorder(BorderFactory.createTitledBorder("All Items Style"));

        fgILabel = new JLabel("  Text Color : ");
        fgI = new ColorControl(this);
        fgI.addPropertyChangeListener(this);

        bgILabel = new JLabel("  Background Color : ");
        bgI = new ColorControl(this);
        bgI.addPropertyChangeListener(this);

        itemPanel.add(fgILabel);
        itemPanel.add(fgI);
        itemPanel.add(bgILabel);
        itemPanel.add(bgI);

        itemPanel2 = new JPanel(new GridLayout(2, 2));
        itemPanel2.setBorder(BorderFactory.createTitledBorder("Selected Item Style"));

        fgSLabel = new JLabel("  Text Color : ");
        fgS = new ColorControl(this);
        fgS.setPreferredSize(new Dimension(30, 30));
        fgS.addPropertyChangeListener(this);

        bgSLabel = new JLabel("  Background Color: ");
        bgS = new ColorControl(this);
        bgS.setPreferredSize(new Dimension(30, 30));
        bgS.addPropertyChangeListener(this);

        itemPanel2.add(fgSLabel);
        itemPanel2.add(fgS);
        itemPanel2.add(bgSLabel);
        itemPanel2.add(bgS);

        pane.setLayout(new GridLayout(4, 0));
        pane.add(fontPanel);
        pane.add(cbPanel);
        pane.add(itemPanel);
        pane.add(itemPanel2);
        this.setSize(300, 400);
    }

    /**
     * ActionListener callback
     */
    public void actionPerformed(ActionEvent e) {
        // Called by the "choose font" button
        fc.setSelectedFont(playlist.getPlaylistFont());
        fc.setVisible(true);
        Font f = fc.getSelectedFont();
        if (f != null) {
            playlist.setPlayListFont(f);
        }
    }

    public void setChange(boolean b) {
        change = b;
    }

    /**
     * Include SubFolders for Drag and Drop
     * 
     * @return
     */
    public boolean getRecursionDnd() {
        return cb1.isSelected();
    }

    /**
     * Is Single Song Mode
     * 
     * @return True if single song mode is active
     */
    public boolean isSingleSongMode() {
        return cb3.isSelected();
    }

    /**
     * Set Single Song Mode
     * 
     * @return True if single song mode is active
     */
    public void setSingleSongMode(boolean b) {
        cb3.setSelected(b);
    }

    /**
     * itemStateChanged
     * 
     * @param e
     *        ItemEvent
     */
    public void itemStateChanged(ItemEvent e) {
        Object list = e.getSource();

        if (list == cb2) {
            if (e.getStateChange() == ItemEvent.SELECTED)
                (playlist.getStatusPanel()).setVisible(true);
            else
                (playlist.getStatusPanel()).setVisible(false);
        }

        // Single Song Mode changed, update playlist prefs
        if (list == cb3) {
            if (e.getStateChange() == ItemEvent.SELECTED)
                playlist.getPreferences().setProperty("singleSongMode", "true");
            else
                playlist.getPreferences().setProperty("singleSongMode", "false");
        }
    }

    /**
     * propertyChange
     * 
     * @param evt
     *        PropertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (change) {
            if (evt.getSource() == fgI)
                playlist.setPlayListFg(fgI.getBackground());
            else if (evt.getSource() == bgI)
                playlist.setPlayListBg(bgI.getBackground());
            else if (evt.getSource() == fgS) {
                playlist.setPlayListSelectionFg(fgS.getBackground());
            }
            else if (evt.getSource() == bgS) playlist.setPlayListSelectionBg(bgS.getBackground());
        }
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
        bgI.setBackground(bgColor);
    }

    public Color getFgColor() {
        return fgColor;
    }

    public void setFgColor(Color fgColor) {
        this.fgColor = fgColor;
        fgI.setBackground(fgColor);
    }

    public Color getSelectionFgColor() {
        return selectionFgColor;
    }

    public void setSelectionFgColor(Color selectionFgColor) {
        this.selectionFgColor = selectionFgColor;
        fgS.setBackground(selectionFgColor);
    }

    public Color getSelectionBgColor() {
        return selectionBgColor;
    }

    public void setSelectionBgColor(Color selectionBgColor) {
        this.selectionBgColor = selectionBgColor;
        bgS.setBackground(selectionBgColor);
    }

    public void setPlaylistFont(String font, int size, int style) {
    // Select font and style by default...

    // select style. plain = 0, bold = 1, italic = 2, bold & italic = 3
    // styles.setSelectedIndex(style);
    }
}
