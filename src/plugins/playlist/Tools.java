package plugins.playlist;

import java.beans.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JButton;
import javakarplayer.plugins.AudioPlugins.taras.FontDialogChoser;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Tools extends JFrame implements  ActionListener, ItemListener, PropertyChangeListener{

  private BorderLayout borderLayout1 = new BorderLayout();
  private Container pane = null;

  private JPanel fontPanel;
  private JPanel cbPanel;
  private JPanel itemPanel;
  private JPanel itemPanel2;

  private JLabel fontLabel;
  private JLabel sizeLabel;
  private JLabel styleLabel;
  private JLabel fgILabel;
  private JLabel bgILabel;
  private JLabel fgSLabel;
  private JLabel bgSLabel;


  private JComboBox fontBox;
  private JComboBox sizes;
  private JComboBox styles;

  private JCheckBox cb1;
  private JCheckBox cb2;

  private ColorControl fgI = null;
  private ColorControl bgI = null;
  private ColorControl fgS = null;
  private ColorControl bgS = null;

  private String fontChoice = "fontChoice";
  private String fontSize;
  private boolean change = false;

  private PlayListPlugin playlist = null;
  private java.awt.Color bgColor;
  private java.awt.Color fgColor;
  private java.awt.Color selectionFgColor;
  private java.awt.Color selectionBgColor;
  private FontDialogChoser fc = new FontDialogChoser();
  private JButton chooseFontButton;

  public Tools(PlayListPlugin pl) {
    super("PlayList configuration");
    playlist = pl;
    try {
      jbInit();

    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  // The window will appear centered
  public void setVisible(boolean flag) {
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

  void jbInit() throws Exception {

    pane = getContentPane();
    pane.setLayout(new GridLayout(4,0));

    fontPanel = new JPanel(new GridLayout(3,2));
    fontPanel.setBorder(BorderFactory.createTitledBorder("Font"));


    /*fontLabel = new JLabel("  Font :");
    GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String envfonts[] = gEnv.getAvailableFontFamilyNames();
    Vector vector = new Vector();
    for ( int i = 1; i < envfonts.length; i++ ) {
      vector.addElement(envfonts[i]);
    }

    fontBox = new JComboBox( vector );
    fontBox.setMaximumRowCount( 9 );
    fontBox.addItemListener(this);
*/

    chooseFontButton = new JButton("Choose Font");
    chooseFontButton.addActionListener(this);

/*    sizeLabel = new JLabel("  Font Size : ");
    sizes = new JComboBox( new Object[]{ "10", "12", "14", "16", "18"} );
    sizes.setMaximumRowCount( 9 );
    sizes.addItemListener(this);

    styleLabel = new JLabel("  Font Style : ");
    styles = new JComboBox( new Object[]{"PLAIN", "BOLD", "ITALIC", "BOLD & ITALIC"});
    styles.setMaximumRowCount( 9 );
    styles.addItemListener(this);
*/
    fontPanel.add(chooseFontButton);
    //fontPanel.add(fontBox);
/*    fontPanel.add(sizeLabel);
    fontPanel.add(sizes);
    fontPanel.add(styleLabel);
    fontPanel.add(styles);
*/
    cb1 = new JCheckBox("Include SubFolders for Drag and Drop", true);
    cb1.addItemListener(this);

    cb2 = new JCheckBox("Show Player Buttons in StatusBar", true);
    cb2.addItemListener(this);

    cbPanel = new JPanel(new GridLayout(4,0));
    cbPanel.add(cb1);
    cbPanel.add(cb2);


    itemPanel = new JPanel(new GridLayout(2,2));
    itemPanel.setBorder(BorderFactory.createTitledBorder("All Items Style"));


    fgILabel = new JLabel("  Text Color : ");
    fgI= new ColorControl(this);
    fgI.addPropertyChangeListener(this);

    bgILabel = new JLabel("  Background Color : ");
    bgI= new ColorControl(this);
    bgI.addPropertyChangeListener(this);

    itemPanel.add(fgILabel);
    itemPanel.add(fgI);
    itemPanel.add(bgILabel);
    itemPanel.add(bgI);

    itemPanel2 = new JPanel(new GridLayout(2,2));
    itemPanel2.setBorder(BorderFactory.createTitledBorder("Selected Item Style"));

    fgSLabel = new JLabel("  Text Color : ");
    fgS= new ColorControl(this);
    fgS.setPreferredSize(new Dimension(30,30));
    fgS.addPropertyChangeListener(this);

    bgSLabel = new JLabel("  Background Color: ");
    bgS= new ColorControl(this);
    bgS.setPreferredSize(new Dimension(30,30));
    bgS.addPropertyChangeListener(this);

    itemPanel2.add(fgSLabel);
    itemPanel2.add(fgS);
    itemPanel2.add(bgSLabel);
    itemPanel2.add(bgS);

    pane.setLayout(new GridLayout(4,0));
    pane.add(fontPanel);
    pane.add(cbPanel);
    pane.add(itemPanel);
    pane.add(itemPanel2);
    this.setSize(300, 400);

  }

  public void actionPerformed(ActionEvent e) {
    // Called by the "choose font" button
    fc.setSelectedFont(playlist.getPlaylistFont());
    fc.setVisible(true);
    Font f = fc.getSelectedFont();
    if(f != null) {
      playlist.setPlayListFont(f);
    }
  }

  public void setChange(boolean b){
    change = b;
  }

  public boolean getRecursionDnd(){
    return cb1.isSelected();
  }

  /**
   * itemStateChanged
   *
   * @param e ItemEvent
   */
  public void itemStateChanged(ItemEvent e) {
    Object list = e.getSource();

    if (list == cb2){
      if (e.getStateChange() == ItemEvent.DESELECTED)
        (playlist.getStatusPanel()).setVisible(false);
      else
        (playlist.getStatusPanel()).setVisible(true);
    }
  }


  /**
   * propertyChange
   *
   * @param evt PropertyChangeEvent
   */
  public void propertyChange(PropertyChangeEvent evt) {
    if (change){
      if (evt.getSource() == fgI)
        playlist.setPlayListFg(fgI.getBackground());
      else if (evt.getSource() == bgI)
        playlist.setPlayListBg(bgI.getBackground());
      else if (evt.getSource() == fgS) {
        playlist.setPlayListSelectionFg(fgS.getBackground());
      }
      else if (evt.getSource() == bgS)
        playlist.setPlayListSelectionBg(bgS.getBackground());
    }

  }
  public java.awt.Color getBgColor() {
    return bgColor;
  }
  public void setBgColor(java.awt.Color bgColor) {
    this.bgColor = bgColor;
    bgI.setBackground(bgColor);
  }
  public java.awt.Color getFgColor() {
    return fgColor;
  }
  public void setFgColor(java.awt.Color fgColor) {
    this.fgColor = fgColor;
    fgI.setBackground(fgColor);

  }
  public java.awt.Color getSelectionFgColor() {
    return selectionFgColor;
  }
  public void setSelectionFgColor(java.awt.Color selectionFgColor) {
    this.selectionFgColor = selectionFgColor;
    fgS.setBackground(selectionFgColor);
  }
  public java.awt.Color getSelectionBgColor() {
    return selectionBgColor;
  }
  public void setSelectionBgColor(java.awt.Color selectionBgColor) {
    this.selectionBgColor = selectionBgColor;
    bgS.setBackground(selectionBgColor);
  }

  public void setPlaylistFont(String font, int size, int style) {
    // Select font and style by default...


    // select style. plain = 0, bold = 1, italic = 2, bold & italic = 3
    //styles.setSelectedIndex(style);
  }
}
