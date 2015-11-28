package com.plarpebu.plugins.basic.playlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.plarpebu.SkinMgr;

/**
 * Playlist Manager
 *
 * @author not attributable
 * @version 1.0
 */
public class PlayListManager extends JFrame implements ActionListener, KeyListener, DropTargetListener {

  private Container pane = null;

  private FileTree filetree = null;

  private JList TrackList = null;

  private JList PlayList = null;

  public DefaultListModel Tracklm = null;

  private DefaultListSelectionModel Tracklsm = null;

  private DefaultListModel Playlm = null;

  private JTextField TrackSearch = null;

  private JTextField PlaySearch = null;

  private Box BoxButton = null;

  private JButton goLeft = null;

  private JButton goRight = null;

  private JButton allLeft = null;

  private JButton allRight = null;

  private JCheckBox caseCb = null;

  private boolean sensitiveCase = false;

  private JButton selection = null;

  private JCheckBox trackSort = null;

  private JPopupMenu popup;

  private PopupListener popupListener;

  /**
   * Constructor
   *
   * @param pl
   */
  public PlayListManager(DefaultListModel pl) {
    Playlm = pl;
    try {
      jbInit();
      setTitle("Playlist Manager");
    } catch(Exception ex) {
      ex.printStackTrace();
    }

    SkinMgr.getInstance().addComponent(this);
  }

  // The window will appear centered
  public void setVisible(boolean flag) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if(frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if(frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    super.setVisible(flag);
  }

  void jbInit() throws Exception {

    // Quelques couleurs
    Color fg1 = new Color(0, 0, 0);
    Color bg2 = new Color(255, 255, 255);
    Color fg2 = new Color(90, 130, 250);

    pane = this.getContentPane();

    BoxButton = Box.createVerticalBox();

    TrackSearch = new JTextField();
    TrackSearch.addKeyListener(this);
    TrackSearch.setMaximumSize(new Dimension(330, 20));
    TrackSearch.setPreferredSize(new Dimension(263, 20));

    Tracklm = new DefaultListModel();
    Tracklsm = new DefaultListSelectionModel();
    Tracklsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    TrackList = new JList(Tracklm);
    TrackList.setSelectionModel(Tracklsm);
    TrackList.setCellRenderer(new MyCellRenderer());
    TrackList.setDragEnabled(true);
    JScrollPane listScrollPane1 = new JScrollPane(TrackList);

    // just in order to refresh
    TrackList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        TrackList.repaint();
      }
    });

    // double-click
    TrackList.addMouseListener(new MyMouseListener());

    TrackList.setForeground(fg1);
    TrackList.setBackground(bg2);
    TrackList.setSelectionBackground(bg2);
    TrackList.setSelectionForeground(fg2);

    Box TrackPanel = Box.createVerticalBox();
    TrackPanel.setBorder(BorderFactory.createTitledBorder("Track List"));
    TrackPanel.setPreferredSize(new Dimension(200, 350));
    TrackPanel.setAlignmentX(CENTER_ALIGNMENT);

    /** *************** Popup TrackList **************************** */

    JButton selectTrack = new JButton("Selection");
    selectTrack.setAlignmentX(CENTER_ALIGNMENT);

    popup = new JPopupMenu("selection");
    JMenuItem mi = new JMenuItem("All");
    mi.setActionCommand("all");
    mi.addActionListener(this);
    popup.add(mi);

    mi = new JMenuItem("None");
    mi.setActionCommand("none");
    mi.addActionListener(this);
    popup.add(mi);

    mi = new JMenuItem("Invert");
    mi.setActionCommand("invert");
    mi.addActionListener(this);
    popup.add(mi);

    // Listener pour le popup
    popupListener = new PopupListener(popup);
    selectTrack.addMouseListener(popupListener);
    /** ************************************************************ */

    TrackPanel.add(TrackSearch /* , BorderLayout.CENTER */);
    TrackPanel.add(listScrollPane1 /* , BorderLayout.SOUTH */);
    TrackPanel.add(selectTrack);

    PlaySearch = new JTextField();
    PlaySearch.addKeyListener(this);
    PlaySearch.setMaximumSize(new Dimension(330, 20));
    PlaySearch.setPreferredSize(new Dimension(263, 20));

    PlayList = new JList(Playlm);
    PlayList.setCellRenderer(new MyCellRenderer());

    // Drag and Drop between the 2 JList
    new DropTarget(PlayList, DnDConstants.ACTION_COPY_OR_MOVE, this, true);

    PlayList.setForeground(fg1);
    PlayList.setBackground(bg2);
    PlayList.setSelectionBackground(bg2);
    PlayList.setSelectionForeground(fg2);

    JScrollPane listScrollPane2 = new JScrollPane(PlayList);

    Box sele = Box.createHorizontalBox();
    sele.setBorder(BorderFactory.createTitledBorder("Selection"));
    selection = new JButton("Selection");
    trackSort = new JCheckBox("Sort");
    sele.add(trackSort);
    sele.add(selection);

    /** ************** Popup PlayList ****************************** */

    JButton selectPlay = new JButton("PlayList");
    selectPlay.setAlignmentX(CENTER_ALIGNMENT);

    popup = new JPopupMenu("PlayList");
    mi = new JMenuItem("Save");
    mi.setActionCommand("save");
    mi.addActionListener(this);
    popup.add(mi);

    mi = new JMenuItem("Load");
    mi.setActionCommand("load");
    mi.addActionListener(this);
    popup.add(mi);

    JSeparator separator = new JSeparator();
    popup.add(separator);

    mi = new JMenuItem("Invert");
    mi.setActionCommand("invert");
    mi.addActionListener(this);
    popup.add(mi);

    popupListener = new PopupListener(popup);
    selectPlay.addMouseListener(popupListener);
    /** ************************************************************ */

    Box PlayPanel = Box.createVerticalBox();
    PlayPanel.setBorder(BorderFactory.createTitledBorder("Play List"));
    PlayPanel.setPreferredSize(new Dimension(200, 350));
    PlayPanel.setAlignmentX(CENTER_ALIGNMENT);
    PlayPanel.add(PlaySearch /* , BorderLayout.CENTER */);
    PlayPanel.add(listScrollPane2 /* , BorderLayout.SOUTH */);
    PlayPanel.add(selectPlay);

    goLeft = new JButton(" < ");
    goLeft.addActionListener(this);
    goRight = new JButton(" > ");
    goRight.addActionListener(this);
    allLeft = new JButton("<<");
    allLeft.addActionListener(this);
    allRight = new JButton(">>");
    allRight.addActionListener(this);
    BoxButton.add(allRight);
    BoxButton.add(goRight);
    BoxButton.add(goLeft);
    BoxButton.add(allLeft);

    Box bb = Box.createHorizontalBox();

    filetree = new FileTree(TrackList);
    bb.add(filetree);
    bb.add(TrackPanel);
    bb.add(BoxButton);
    bb.add(PlayPanel);

    Box options = Box.createHorizontalBox();

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Options"));
    caseCb = new JCheckBox("Sensitive Case", false);
    caseCb.addActionListener(this);
    panel.add(caseCb);
    options.add(panel);

    pane.add(bb, BorderLayout.CENTER);
    pane.add(options, BorderLayout.SOUTH);
    pack();
  }

  /**
   * actionPerformed
   *
   * @param e ActionEvent
   */
  public void actionPerformed(ActionEvent e) {

    String c = e.getActionCommand();
    if(e.getSource() == goRight) {
      int[] indices = TrackList.getSelectedIndices();
      for(int i = 0; i < indices.length; i++) {
        Playlm.addElement(Tracklm.getElementAt(indices[i]));
      }
    } else if(e.getSource() == goLeft) {
      int[] indices = PlayList.getSelectedIndices();
      for(int i = 0; i < indices.length; i++) {
        Playlm.removeElementAt(indices[i] - i);
      }
    } else if(e.getSource() == allRight) {
      for(int i = 0; i < Tracklm.size(); i++) {
        Playlm.addElement(Tracklm.getElementAt(i));
      }
    } else if(e.getSource() == allLeft) {
      Playlm.removeAllElements();
    } else if(e.getSource() == caseCb) {
      sensitiveCase = caseCb.isSelected();
    }
    // Traitement de selection sur la TrackList
    else if(c.equals("all")) {
      int taille = Tracklm.size();
      if(taille > 0) {
        TrackList.clearSelection();
        Tracklsm.setSelectionInterval(0, taille - 1);
      }
    } else if(c.equals("none")) {
      Tracklsm.clearSelection();
      Tracklsm.setAnchorSelectionIndex(-1);
      Tracklsm.setLeadSelectionIndex(-1);
    } else if(c.equals("invert")) {
      int[] indices = TrackList.getSelectedIndices();
      int taille = Tracklm.size();
      if(indices.length == 1) {
        Tracklsm.clearSelection();
        Tracklsm.addSelectionInterval(0, indices[0] - 1);
        if(indices[0] != taille - 1) {
          Tracklsm.addSelectionInterval(indices[0] + 1, taille - 1);
        }
      } else if(indices.length > 1) {
        Tracklsm.clearSelection();
        if(indices[0] > 0) {
          Tracklsm.addSelectionInterval(0, indices[0] - 1);
        }
        if(indices[indices.length - 1] < taille) {
          Tracklsm.addSelectionInterval(indices[indices.length - 1] + 1, taille - 1);
        }
        int borne_a = indices[0], borne_b = -1;
        for(int i = 0; i + 1 < indices.length; i++) {
          if(indices[i] + 1 == (indices[i + 1])) {
            borne_a = indices[i + 1];
          } else {
            borne_b = indices[i + 1];
            Tracklsm.addSelectionInterval(borne_a + 1, borne_b - 1);
            borne_a = indices[i + 1];
          }
        }
      }
    }
  }

  /**
   * keyPressed
   *
   * @param e KeyEvent
   */
  public void keyPressed(KeyEvent e) {
  }

  /**
   * keyReleased
   *
   * @param e KeyEvent
   */
  public void keyReleased(KeyEvent e) {
    Object o = e.getSource();
    if(o == TrackSearch) {
      String chaine = TrackSearch.getText();
      int taille = chaine.length();
      for(int i = 0; i < Tracklm.size(); i++) {
        String old = ((File) Tracklm.getElementAt(i)).getName();
        int oldTaille = old.length();
        if(taille > 1 && taille <= oldTaille && (sensitiveCase ? (old.indexOf(chaine) != -1) : (old.toLowerCase()
                                                                                                   .indexOf(chaine) != -1))) {
          TrackList.setSelectedIndex(i);
          TrackList.ensureIndexIsVisible(i);
        }
      }
    } else if(o == PlaySearch) {
      String chaine = PlaySearch.getText();
      int taille = chaine.length();
      for(int i = 0; i < Playlm.size(); i++) {
        String old = ((File) Playlm.getElementAt(i)).getName();
        int oldTaille = old.length();
        if(taille > 1 && taille <= oldTaille && (sensitiveCase ? (old.indexOf(chaine) != -1) : (old.toLowerCase()
                                                                                                   .indexOf(chaine) != -1))) {
          PlayList.setSelectedIndex(i);
          PlayList.ensureIndexIsVisible(i);
        }
      }
    }

  }

  /**
   * keyTyped
   *
   * @param e KeyEvent
   */
  public void keyTyped(KeyEvent e) {
  }

  /**
   * dragEnter
   *
   * @param e DropTargetDragEvent
   */
  public void dragEnter(DropTargetDragEvent e) {
    if(isDragOk(e) == false) {
      e.rejectDrag();
      return;
    }
  }

  /**
   * dragOver
   *
   * @param e DropTargetDragEvent
   */
  public void dragOver(DropTargetDragEvent e) {
    if(isDragOk(e) == false) {
      e.rejectDrag();
      return;
    }
  }

  /**
   * dragExit
   *
   * @param e DropTargetEvent
   */
  public void dragExit(DropTargetEvent e) {
  }

  /**
   * dropActionChanged
   *
   * @param e DropTargetDragEvent
   */
  public void dropActionChanged(DropTargetDragEvent e) {
    if(isDragOk(e) == false) {
      e.rejectDrag();
      return;
    }
  }

  /**
   * ajoutes les fichiers � la playlist lors du drop nous pouvons ajouter un ou plusieurs fichier
   * venant de l'exterieur ainsi que des playlist
   *
   * @param e DropTargetDropEvent
   */
  public void drop(DropTargetDropEvent e) {
    // Verification DataFlavor
    DataFlavor[] dfs = e.getCurrentDataFlavors();
    DataFlavor tdf = null;
    for(int i = 0; i < dfs.length; i++) {
      if(DataFlavor.javaFileListFlavor.equals(dfs[i]) || DataFlavor.stringFlavor.equals(dfs[i])) {
        tdf = dfs[i];
        break;
      }
    }
    /** si on a trouve une bonne dataFlavor */
    if(tdf != null) {
      /** et que l'on souhite faire une copie */
      if((e.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
        /** le drop est accepte */
        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      } else {
        return;
      }
      try {
        Transferable t = e.getTransferable();
        Object data = t.getTransferData(tdf);

        if(data instanceof java.util.List) {
          java.util.List al = (java.util.List) data;

          if(al.size() > 0) {
            File file = null;

            /**
             * ajout des donn�es recues dans la liste graphique et r�elle
             */
            ListIterator li = al.listIterator();
            while(li.hasNext()) {
              file = (File) li.next();
              /** s'il s'agit de chansons */
              if(file.isFile()) {
                /** s'il s'agit des chansons * */
                if((file != null && file.getAbsolutePath().endsWith(".mp3")) || (file != null && file.getAbsolutePath()
                                                                                                     .endsWith(".kar")) || (file != null && file
                  .getAbsolutePath().endsWith(".mid")) || (file != null && file.getAbsolutePath().endsWith(".zip"))) {
                  Playlm.addElement(file);
                }
              } else if(file.isDirectory()) {
                parcoursRecursif(file, true, 0);
              }

            }
          }
        }
        /** On parse la string pour en extraire chaque fichier * */
        else if(data instanceof java.lang.String) {
          parseData((String) data);
        }
      } catch(IOException ioe) {
        e.dropComplete(false);
        return;
      } catch(UnsupportedFlavorException ufe) {
        e.dropComplete(false);
        return;
      } catch(Exception ex) {
        e.dropComplete(false);
        return;
      }
      e.dropComplete(true);
    }
  }

  public void parseData(String data) {
    StringTokenizer st = new StringTokenizer(data, "\n");
    while(st.hasMoreTokens()) {
      Playlm.addElement(new File(st.nextToken()));
    }
  }

  public void parcoursRecursif(File f, boolean recu, int depth) {
    if(depth > PlayListPlugin.MAXDEPTH) {
      System.out.println("Stopping recursion, MAXDEPTH = " + PlayListPlugin.MAXDEPTH + " reached");
      return;
    }

    File tab[] = f.listFiles();
    String path;
    for(int i = 0; i < tab.length; i++) {
      path = tab[i].getAbsolutePath();
      if((tab[i].isFile() && path.endsWith(".mp3")) || (tab[i].isFile() && path.endsWith(".kar")) || (tab[i]
        .isFile() && path.endsWith(".mid")) || (tab[i].isFile() && path.endsWith(".zip"))) {
        Playlm.addElement(tab[i]);
      } else if(tab[i].isDirectory()) {
        if(!(tab[i].getName().equals("System Volume Information")) && recu) {
          parcoursRecursif(tab[i], recu, depth + 1);
        }
      } else {
        ;
      }
    }
  }

  /**
   * isDragOk : v�rifie si le DnD est possible il est possible si on essaie de "droper" soit un
   * fichier soit une String
   *
   * @param e DropTargetDragEvent
   * @return boolean
   */
  private boolean isDragOk(DropTargetDragEvent e) {
    // verification DataFlavor
    DataFlavor[] dfs = e.getCurrentDataFlavors();
    DataFlavor tdf = null;
    for(int i = 0; i < dfs.length; i++) {
      /**
       * si on essaie d'ajouter un fichier ou du texte, cad un path pour un fichier mp3
       */
      if(DataFlavor.javaFileListFlavor.equals(dfs[i]) || DataFlavor.stringFlavor.equals(dfs[i])) {
        tdf = dfs[i];
        break;
      }
    }
    if(tdf != null) {
      if((e.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
        return true;
      }

      return false;
    }

    return false;
  }

  public void setSupportedFileFormats(String[] exts) {
    filetree.setSupportedFileFormats(exts);
  }

  /** ************************************************************************** */
  /** ************************************************************************** */
  /** ************************************************************************** */

  /**
   * Classe d�di�e au Popup de la JList
   */

  class PopupListener extends MouseAdapter {

    private JPopupMenu ppm = null;

    public PopupListener(JPopupMenu ppm) {
      this.ppm = ppm;
    }

    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);

    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    /**
     * Fonction qui initialise le PopUp lors de son affichage i.e. qui active ou d�sactive
     * certains menus selon le contexte
     */

    public void maybeShowPopup(MouseEvent e) {
      ppm.show(e.getComponent(), e.getX(), e.getY());
      ppm.revalidate();
      // }
    }
  }

  /** ************************************************************************** */
  /** ************************************************************************** */
  /**
   * *************************************************************************
   */

  class MyMouseListener extends MouseAdapter {

    public void mouseClicked(MouseEvent e) {
      if(e.getClickCount() == 2) {
        int index = TrackList.getSelectedIndex();
        if(index != -1) {
          Playlm.addElement(Tracklm.getElementAt(index));
        }
      }
    }
  }

}
