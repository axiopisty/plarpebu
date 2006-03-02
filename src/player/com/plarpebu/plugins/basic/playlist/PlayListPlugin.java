package com.plarpebu.plugins.basic.playlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Insets;
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
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;

import com.l2fprod.common.swing.JDirectoryChooser;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicController;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicPlayerEvent;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicPlayerException;
import com.plarpebu.javazoom.jlgui.basicplayer.BasicPlayerListener;
import com.plarpebu.plugins.sdk.FramePlugin;

/**
 * Playlist plugin
 * 
 * @author kmschmidt
 */
public class PlayListPlugin extends FramePlugin implements BasicPlayerListener, ActionListener, MouseListener,
MouseMotionListener, DropTargetListener {

    private FileDialog fd;

    private JFileChooser fc = null;

    private JDirectoryChooser dc = new JDirectoryChooser();

    private JButton buttonAdd;

    private JButton buttonPlay;

    private JButton buttonStop;

    private JButton buttonPause;

    private JButton buttonNext;

    private JButton buttonPrev;

    private JCheckBox cb;

    private JLabel time;

    private JPanel pnl;

    private JMenuBar menuBar;

    private JMenu menu;

    private JMenuItem menuItem;

    private ToolTipManager tpm;

    private JSlider slider;

    private DefaultBoundedRangeModel model = null;

    private int modelscale = 100;

    private int byteslength = -1;

    private int milliseconds = -1;

    private static JList lstPlayList;

    private static DefaultListModel listModel;

    private JPopupMenu popup;

    private static BasicController controller = null;

    private int oldRand = -1;

    private PopupListener popupListener;

    private static boolean shuffle = false;

    private static boolean playing = false;

    private static boolean paused = false;

    private int indexSelected = -1;

    private int currentSelection = -1;

    private String fontName = "Arial";

    private int fontSize = 10;

    private int fontStyle = 0;

    private String lastDirSelected, lastFileSelected;

    private Tools toolFrame = null;

    private PlayListManager plm = null;

    private boolean recursion = true;

    // For recursive exploration
    public static final int MAXDEPTH = 4;

    // Playlist
    private String currentPlaylistFilename;

    private MyCellRenderer myCellRenderer = new MyCellRenderer();

    /**
     * Constructeur de la classe PlayList
     */
    public PlayListPlugin() {
        // Read preferences
        readPreferences();

        // Quelques couleurs
        Color fg1 = new Color(255, 255, 255);
        Color bg2 = new Color(0, 0, 0);
        Color fg2 = new Color(90, 130, 250);

        /* fd = new FileDialog(this); */
        addMouseListener(this);
        addMouseMotionListener(this);

        listModel = new DefaultListModel();
        lstPlayList = new JList(listModel);
        lstPlayList.setCellRenderer(myCellRenderer);
        lstPlayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstPlayList.setSelectedIndex(0);
        lstPlayList.setDragEnabled(true);

        // Drag and drop
        new DropTarget(lstPlayList, DnDConstants.ACTION_COPY_OR_MOVE, this, true);

        lstPlayList.setForeground(fg2);
        lstPlayList.setBackground(bg2 /* Color.getColor("TRANSLUCENT") */);
        lstPlayList.setSelectionBackground(bg2);
        lstPlayList.setSelectionForeground(fg1);
        lstPlayList.addMouseListener(this);
        JScrollPane listScrollPane = new JScrollPane(lstPlayList);

        tpm = ToolTipManager.sharedInstance();
        tpm.registerComponent(lstPlayList);
        // System.out.println(tpm.isEnabled() + " " + tpm.getReshowDelay());
        tpm.setReshowDelay(100);
        pnl = new JPanel();

        buttonPrev = addButton(pnl, "prev", "prev", "/icons/tmp/littlePrev.gif");
        buttonPrev.addActionListener(this);

        buttonPlay = addButton(pnl, "play", "play", "/icons/tmp/littlePlay.gif");
        buttonPlay.addActionListener(this);

        buttonPause = addButton(pnl, "pause", "pause", "/icons/tmp/littlePause.gif");
        buttonPause.addActionListener(this);

        buttonStop = addButton(pnl, "stop", "stop", "/icons/tmp/littleStop.gif");
        buttonStop.addActionListener(this);

        buttonNext = addButton(pnl, "next", "next", "/icons/tmp/littleNext.gif");
        buttonNext.addActionListener(this);

        buttonAdd = addButton(pnl, "add", "add file", "/icons/tmp/littleEject.gif");
        buttonAdd.addActionListener(this);

        model = new DefaultBoundedRangeModel(0, 1, 0, modelscale);
        slider = new JSlider(model);
        slider.setPreferredSize(new Dimension(100, 15));
        slider.setEnabled(false);
        slider.addMouseListener(this);
        pnl.add(slider);

        time = new JLabel("00:00 / 00:00");
        pnl.add(time);

        Container pane = this.getContentPane();
        pane.setLayout(new BorderLayout());

        popup = new JPopupMenu("FileInfo");
        JMenuItem mi = new JMenuItem("Play Item");
        mi.setActionCommand("playitem");
        mi.addActionListener(this);
        popup.add(mi);
        popup.addSeparator();

        mi = new JMenuItem("Crop Item");
        mi.setActionCommand("cropitem");
        mi.addActionListener(this);
        popup.add(mi);

        mi = new JMenuItem("Remove Item");
        mi.setActionCommand("removeitem");
        mi.addActionListener(this);
        popup.add(mi);

        popup.addSeparator();
        mi = new JMenuItem("File Info");
        mi.setActionCommand("fileinfo");
        mi.addActionListener(this);
        popup.add(mi);

        pane.add(listScrollPane, BorderLayout.CENTER);
        pane.add(pnl, BorderLayout.SOUTH);

        popupListener = new PopupListener();
        lstPlayList.addMouseListener(popupListener);
        lstPlayList.addMouseMotionListener(this);

        menuBar = new JMenuBar();

        menu = new JMenu("Add");
        menu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(menu);

        menuItem = new JMenuItem("Add files");
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("addfile");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Add dir");
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("adddir");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu = new JMenu("Remove");
        menu.setMnemonic(KeyEvent.VK_R);
        menuBar.add(menu);

        menuItem = new JMenuItem("Remove song");
        menuItem.setMnemonic(KeyEvent.VK_M);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("remove");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Remove all");
        menuItem.setMnemonic(KeyEvent.VK_V);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("clear");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Crop file");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("cropitem");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu = new JMenu("List");
        menu.setMnemonic(KeyEvent.VK_L);
        menuBar.add(menu);

        menuItem = new JMenuItem("Load PlayList");
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("load");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Save playlist");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuItem.setActionCommand("save");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu = new JMenu("Tools");
        menu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(menu);

        menuItem = new JMenuItem("Config");
        menuItem.setActionCommand("config");
        menuItem.setMnemonic(KeyEvent.VK_F);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Manager");
        menuItem.setActionCommand("manager");
        menuItem.setMnemonic(KeyEvent.VK_G);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
        menuItem.addActionListener(this);
        menu.add(menuItem);

        toolFrame = new Tools(this);
        plm = new PlayListManager(listModel);
        this.setJMenuBar(menuBar);
        this.setSize(350, 230);
        this.setTitle("PlayList " + getVersion());

        // Load preferences
        loadPreferences();
    }

    /**
     * Load preferences
     * 
     * @throws NumberFormatException
     * @throws HeadlessException
     */
    private void loadPreferences() throws NumberFormatException, HeadlessException {
        try {
            // Look for the currentPlaylist
            currentPlaylistFilename = preferences.getProperty("currentPlaylist");
            // System.out.println("Lu dans les prefs currentPlaylist = " +
            // currentPlaylistFilename);
            readPlaylist(currentPlaylistFilename);

            // Look for font used in the playlist
            String font = preferences.getProperty("font");
            int size = Integer.parseInt(preferences.getProperty("fontSize"));
            int style = Integer.parseInt(preferences.getProperty("fontStyle"));
            setPlayListFont(font, size, style);

            // Look for text colors
            Color c = StringToColor(preferences.getProperty("foregroundColor"));
            setPlayListFg(c);
            toolFrame.setFgColor(c);

            c = StringToColor(preferences.getProperty("backgroundColor"));
            setPlayListBg(c);
            toolFrame.setBgColor(c);

            c = StringToColor(preferences.getProperty("selectionForegroundColor"));
            setPlayListSelectionFg(c);
            toolFrame.setSelectionFgColor(c);
            System.out.println("foreground selection = " + colorToString(c));

            c = StringToColor(preferences.getProperty("selectionBackgroundColor"));
            setPlayListSelectionBg(c);
            toolFrame.setSelectionBgColor(c);

            // Load tool prefs
            if (preferences.getProperty("includeSubFolderForDragAndDrop").equals("true"))
                setIncludeSubFolderForDragAndDrop(true);
            else
                setIncludeSubFolderForDragAndDrop(false);

            if (preferences.getProperty("showPlayerButtonsInStatusBar").equals("true"))
                setShowPlayerButtonsInStatusBar(true);
            else
                setShowPlayerButtonsInStatusBar(false);

            if (preferences.getProperty("singleSongMode").equals("true"))
                setSingleSongMode(true);
            else
                setSingleSongMode(false);

            if (preferences.getProperty("showLineNumbersInPlayList").equals("true"))
                setShowLineNumbersInPlayList(true);
            else
                setShowLineNumbersInPlayList(false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return "PlayList";
    }

    public String getVersion() {
        return "v1.0";
    }

    /**
     * Include SubFolders for Drag and Drop
     * 
     * @return
     */
    public boolean isIncludeSubFolderForDragAndDrop() {
        return preferences.getProperty("includeSubFolderForDragAndDrop").equals("true");
    }

    /**
     * Set include SubFolders for Drag and Drop
     */
    public void setIncludeSubFolderForDragAndDrop(boolean b) {
        if (b)
            preferences.setProperty("includeSubFolderForDragAndDrop", "true");
        else
            preferences.setProperty("includeSubFolderForDragAndDrop", "false");
    }

    /**
     * Is Show player Buttons In Status Bar
     * 
     * @return True if showing player buttons
     */
    public boolean isShowPlayerButtonsInStatusBar() {
        return preferences.getProperty("showPlayerButtonsInStatusBar").equals("true");
    }

    /**
     * Set Show player Buttons In Status Bar
     */
    public void setShowPlayerButtonsInStatusBar(boolean b) {
        if (b)
            preferences.setProperty("showPlayerButtonsInStatusBar", "true");
        else
            preferences.setProperty("showPlayerButtonsInStatusBar", "false");

        getStatusPanel().setVisible(b);
    }

    /**
     * Is Single Song Mode
     * 
     * @return True if single song mode is active
     */
    public boolean isSingleSongMode() {
        return preferences.getProperty("singleSongMode").equals("true");
    }

    /**
     * Set Single Song Mode
     */
    public void setSingleSongMode(boolean b) {
        if (b)
            preferences.setProperty("singleSongMode", "true");
        else
            preferences.setProperty("singleSongMode", "false");
    }

    /**
     * Is Show Line Numbers in Playlist
     * 
     * @return True if showing line numbers in playlist
     */
    public boolean isShowLineNumbersInPlayList() {
        return preferences.getProperty("showLineNumbersInPlayList").equals("true");
    }

    /**
     * Set Show Line Numbers in Playlist
     */
    public void setShowLineNumbersInPlayList(boolean b) {
        if (b)
            preferences.setProperty("showLineNumbersInPlayList", "true");
        else
            preferences.setProperty("showLineNumbersInPlayList", "false");

        myCellRenderer.setShowLineNumbers(b);
    }

    public void setPlayListFont(String fontName, int fontSize, int fontStyle) {
        lstPlayList.setFont(new Font(fontName, fontStyle, fontSize));
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        preferences.setProperty("font", fontName);
        preferences.setProperty("fontSize", "" + fontSize);
        preferences.setProperty("fontStyle", "" + fontStyle);
    }

    public void setPlayListFont(String s) {
        lstPlayList.setFont(new Font(s, fontStyle, fontSize));
        fontName = s;
        preferences.setProperty("font", s);
    }

    public void setPlayListFont(Font f) {
        lstPlayList.setFont(f);

        fontName = f.getName();
        fontSize = f.getSize();
        fontStyle = f.getStyle();
        preferences.setProperty("font", fontName);
        preferences.setProperty("fontSize", "" + fontSize);
        preferences.setProperty("fontStyle", "" + fontStyle);
    }

    public Font getPlaylistFont() {
        return lstPlayList.getFont();
    }

    public void setPlayListSize(int s) {
        lstPlayList.setFont(new Font(fontName, fontStyle, s));
        fontSize = s;
        preferences.setProperty("fontSize", "" + s);
    }

    public void setPlayListStyle(int s) {
        lstPlayList.setFont(new Font(fontName, s, fontSize));
        fontStyle = s;
        preferences.setProperty("fontStyle", "" + s);
    }

    public void setPlayListFg(Color c) {
        lstPlayList.setForeground(c);
        preferences.setProperty("foregroundColor", colorToString(c));
    }

    public void setPlayListBg(Color c) {
        lstPlayList.setBackground(c);
        preferences.setProperty("backgroundColor", colorToString(c));
    }

    public void setPlayListSelectionFg(Color c) {
        System.out.println("On met sel fg à " + colorToString(c));
        lstPlayList.setSelectionForeground(c);
        preferences.setProperty("selectionForegroundColor", colorToString(c));
    }

    public void setPlayListSelectionBg(Color c) {
        lstPlayList.setSelectionBackground(c);
        preferences.setProperty("selectionBackgroundColor", colorToString(c));
    }

    public static File getFileSelected() {
        int index = lstPlayList.getSelectedIndex();
        return (File) listModel.get(index);
    }

    public static void setSelectedFile() {
        lstPlayList.setSelectedIndex(0);
    }

    public static void setPlaying(boolean v) {
        playing = v;
    }

    public JFrame getToolFrame() {
        return toolFrame;
    }

    public JFrame getManagerFrame() {
        return plm;
    }

    public JPanel getStatusPanel() {
        return pnl;
    }

    /**
     * Permet de creer un nouveau bouton en lui associant, une action, un
     * tooltip et une icone
     * 
     * @param p
     *        composant dans lequel il va etre ajouté
     * @param name
     *        son nom pour lui associer une action(actionPerformed)
     * @param tooltiptext
     *        le tooltip a afficher
     * @param imageName
     *        le chemin d'accés a l'icone
     */
    JButton addButton(JComponent p, String name, String tooltiptext, String imageName) {
        JButton b;
        if ((imageName == null) || (imageName.equals(""))) {
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
            }
        }

        b.setToolTipText(tooltiptext);
        Insets insets = new Insets(0, 0, 0, 0);
        b.setMargin(insets);

        return b;
    }

    /**
     * Permet de creer un nouveau Jtoggle bouton en lui associant, une action,
     * un tooltip et une icone
     * 
     * @param p
     *        composant dans lequel il va etre ajouté
     * @param name
     *        son nom pour lui associer une action(actionPerformed)
     * @param tooltiptext
     *        le tooltip a afficher
     * @param imageName
     *        le chemin d'accés a l'icone
     */
    JToggleButton addToggleButton(JComponent p, String name, String tooltiptext, String imageName) {
        JToggleButton b;
        if ((imageName == null) || (imageName.equals(""))) {
            b = (JToggleButton) p.add(new JToggleButton(name));
        }
        else {
            java.net.URL u = this.getClass().getResource(imageName);
            if (u != null) {
                ImageIcon im = new ImageIcon(u);

                b = (JToggleButton) p.add(new JToggleButton(im));
            }
            else {
                b = (JToggleButton) p.add(new JToggleButton(name));
                // b.setActionCommand(name);
            }
        }

        b.setToolTipText(tooltiptext);
        Insets insets = new Insets(0, 0, 0, 0);
        b.setMargin(insets);

        return b;
    }

    public void setController(BasicController newController) {
        PlayListPlugin.controller = newController;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean newValue) {
        shuffle = newValue;
    }

    public int getIndexShuffle() {
        int index = listModel.getSize();
        int val;
        Random rand = new Random();
        while ((val = rand.nextInt(index)) == oldRand) {
            ;
        }
        oldRand = val;
        return val;
    }

    public static void getNext() {
        paused = false;
        /*
         * if (shuffle) { int newVal = getIndexShuffle();
         * lstPlayList.setSelectedIndex(newVal); } else {
         */
        int taille = listModel.size();
        int index = lstPlayList.getSelectedIndex();
        if (index + 1 < taille) {
            lstPlayList.setSelectedIndex(index + 1);
            lstPlayList.ensureIndexIsVisible(index + 1);
            if (playing) {
                try {
                    controller.open((File) listModel.elementAt(index + 1));
                    controller.play();
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
            }
        }
        else {
            lstPlayList.setSelectedIndex(0);
            lstPlayList.ensureIndexIsVisible(0);
            if (playing) {
                try {
                    controller.open((File) listModel.elementAt(0));
                    controller.play();
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
            }
        }
    }

    public static void getPrev() {
        paused = false;
        /*
         * if (shuffle) { int newVal = getIndexShuffle();
         * lstPlayList.setSelectedIndex(newVal); } else {
         */
        int taille = listModel.size();
        int index = lstPlayList.getSelectedIndex();
        if (index - 1 < 0) {
            lstPlayList.setSelectedIndex(taille - 1);
            lstPlayList.ensureIndexIsVisible(taille - 1);
            if (playing) {
                try {
                    controller.open((File) listModel.elementAt(taille - 1));
                    controller.play();
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
            }
        }
        else {
            lstPlayList.setSelectedIndex(index - 1);
            lstPlayList.ensureIndexIsVisible(index - 1);
            if (playing) {
                try {
                    controller.open((File) listModel.elementAt(index - 1));
                    controller.play();
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
            }
        }
    }

    /**
     * Cette methode va permettre a l'utilisateur de sauvegarder la playlist
     * actuelle en mode texte [lisible donc par l'homme ]. Les 3 premieres
     * lignes indiqueront la fonte , taille et style de la playlist (au niveau
     * ecriture) ensuite les lignes suivantes seront composees du titre des
     * chansons avec son numero de lecture. Notons que l'interface de dialogue
     * avec les fichiers conservent le dernier choix (repertoire + fichier) pour
     * pouvoir a une prochaine ouverture de la boite de dialogue etre
     * directement au dernier endroit selectionne.
     */
    public void savePlaylist() {
        FileDialog fd = new FileDialog(this, "Save playlist", FileDialog.SAVE);
        fd.setDirectory(lastDirSelected);
        fd.setFile(lastFileSelected);
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String file = fd.getFile();
        lastDirSelected = (dir == null) ? lastDirSelected : dir;
        lastFileSelected = (file == null) ? lastFileSelected : file;

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(dir + file));
            PrintWriter pw = new PrintWriter(bw);
            for (int x = 0; x < listModel.getSize(); x++) {
                pw.write(x + 1 + "|" + listModel.getElementAt(x));
                bw.newLine();
            }
            pw.close();
            bw.close();

            // Save current playlist filename in the preferences
            preferences.setProperty("currentPlaylist", dir + file);

        }
        catch (IOException ioex) {
            System.out.println("Unable to write playlist to disk");
        }
    }

    /**
     * Cette methode va permettre a l'utilisateur de charger une playlist
     * [lisible donc par l'homme ]. Les 3 premieres lignes indiqueront la fonte ,
     * taille et style de la playlist (au niveau ecriture) ensuite les lignes
     * suivantes seront composees du titre des chansons avec son numero de
     * lecture. Notons que l'interface de dialogue avec les fichiers conservent
     * le dernier choix (repertoire + fichier) pour pouvoir a une prochaine
     * ouverture de la boite de dialogue etre directement au dernier endroit
     * selectionne. Si le fichier selectionne n'est pas un fichier conforme a
     * une playlist alors le systeme nous l'indique et ne charge donc aucune
     * playlist.
     */
    public void loadPlaylist() {
        if (fd == null) fd = new FileDialog(this, "Load playlist", FileDialog.LOAD);
        fd.setDirectory(lastDirSelected);
        fd.setFile(lastFileSelected);
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String file = fd.getFile();
        lastDirSelected = (dir == null) ? lastDirSelected : dir;
        lastFileSelected = (file == null) ? lastFileSelected : file;

        readPlaylist(dir + file);
    }

    private void readPlaylist(String filename) throws HeadlessException, NumberFormatException {
        String line = "";
        StringTokenizer st = null;
        String currentDir = null;
        String absolutePlaylistFilename = null;
        String absoluteSongFilename = null;
        boolean demo_playlist = false;

        // Default case is when the playlist filename is absolute
        absolutePlaylistFilename = filename;
        currentDir = System.getProperty("user.dir");

        if (filename.endsWith("demoPlaylist.pls")) {
            demo_playlist = true;
            // The case where filename equals exactly "demoPlaylist.pls is when
            // the filename has been read from the
            // defaultPlaylistPlugin.properties file
            // In that case, we dont't know where the software has been
            // installed and we cannot use
            // an absolute filename.
            // If the filename is not equal but ends with "demoPlaylist.pls"
            // then it is the case
            // when the user lauched the software for the first time, opened the
            // default demo playlist
            // and then quit. Preferences have been saved and the default
            // playlist is an absolute
            // filename. But in that case, the songs are relative... this is why
            // we have to test the two cases...
            if (filename.equals("demoPlaylist.pls")) {
                // This is a relative filename, add the current dir before the
                // filename
                absolutePlaylistFilename = currentDir + File.separator + "demo" + File.separator + filename;
            }
        }

        File f = new File(absolutePlaylistFilename);

        if (!f.exists()) {
            System.out.println("file " + absolutePlaylistFilename + " does not exists");
        }

        if (f.exists()) {
            listModel.clear();
            try {
                BufferedReader r = new BufferedReader(new FileReader(f));

                // The song's filenames
                while ((line = r.readLine()) != null) {
                    if (demo_playlist) {
                        // All filenames are relative
                        absoluteSongFilename = currentDir + File.separator + line;
                    }
                    else {
                        st = new StringTokenizer(line, "|");
                        st.nextToken();
                        // We are after the first number
                        absoluteSongFilename = st.nextToken();
                    }
                    addElementToPlayList(absoluteSongFilename);
                }

                r.close();
                // toolFrame.setChange(true);

                // Save current playlist filename in the preferences
                preferences.setProperty("currentPlaylist", absolutePlaylistFilename);
            }
            catch (IOException ioex) {
                System.out.println("Unable to read in playlist");
            }
            catch (NumberFormatException nfex) {
                JOptionPane.showMessageDialog(this, "Invalid playlist format ");
            }
        }
    }

    public void parcoursRecursif(File f, boolean recu, int depth) {
        if (depth > MAXDEPTH) {
            System.out.println("Stopping recursion, MAXDEPTH = " + MAXDEPTH + " reached");
            return;
        }

        File tab[] = f.listFiles();
        String path;
        for (int i = 0; i < tab.length; i++) {
            path = tab[i].getAbsolutePath();
            // System.out.println("PATH = " + path);
            if (tab[i].isFile() && controller.isFileSupported(path)) {
                listModel.addElement(tab[i]);
            }
            else if (tab[i].isDirectory()) {
                // System.out.println("RECURSIF0");
                if (!(tab[i].getName().equals("System Volume Information")) && recu) {
                    // System.out.println("RECURSIF");
                    parcoursRecursif(tab[i], recu, depth + 1);
                }
            }
            else
                ;
        }
    }

    /**
     * Cette procedure va nous permettre d'ajouter un fihier dansla playliste
     * 
     * @param name
     *        le nom du fichier a ajouter dans la playliste
     */
    public static void addElementToPlayList(String name) {
        File f = new File(name);
        // System.out.println("On ajoute file = " + f.getAbsolutePath());
        listModel.addElement(f);
    }

    /***************************************************************************
     * Permet de gerer une bonne partie des evenements notamment la gestion des
     * boutons et du menu du popup
     * 
     * @param e
     *        L'action event
     **************************************************************************/
    public void actionPerformed(ActionEvent e) {
        String c = e.getActionCommand();

        // Ajout d'un fichier à partir du menu
        if (e.getSource() == buttonAdd || c.equals("addfile")) {

            if (fc == null) fc = new JFileChooser();

            fc.setMultiSelectionEnabled(true);
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setDialogTitle("Add a file");
            // MP3FileFilter mp3filter = new MP3FileFilter();
            // fc.setFileFilter(mp3filter);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles(); // Attention gerer le
                // cas ou il ne renvoie
                // rien
                //
                for (int i = 0; i < files.length; i++) {
                    if (controller.isFileSupported(files[i])) {
                        listModel.addElement(files[i]);
                    }
                }
            }
            if (listModel.size() == 1) {
                lstPlayList.setSelectedIndex(0);
            }
        }

        // Ajout d'un repertoire (avec inclusion recursive optionnelle
        // des sous-repertoires) à partir du menu
        else {
            if (e.getSource() == buttonAdd || c.equals("adddir")) {

                // Ajout d'une petite option
                cb = new JCheckBox("Include subdirs, ctrl = multiselection", recursion);
                cb.addActionListener(this);

                dc.setAccessory(cb);
                dc.setMultiSelectionEnabled(true);
                // dc.addActionListener (this);

                int choice = dc.showOpenDialog(null);
                if (choice == JDirectoryChooser.APPROVE_OPTION) {
                    File[] selectedFiles = dc.getSelectedFiles();
                    for (int i = 0, l = selectedFiles.length; i < l; i++) {
                        parcoursRecursif(selectedFiles[i], recursion, 0);
                    }
                }

                // parcoursRecursif(file, recursion);
            }

            // Suppression de la selection courante à partir du
            // menu du popup ou à partir du menu
            else if (c.equals("remove")) {
                if (listModel.size() != 0 && !(lstPlayList.isSelectionEmpty())) {
                    int index = lstPlayList.getSelectedIndex();
                    listModel.remove(index);
                }
            }

            // Play pour une chanson à partir du bouton de la barre de status
            // ou à partir du menu du popup
            else if (e.getSource() == buttonPlay) {
                try {
                    if (listModel.size() != 0 && !(lstPlayList.isSelectionEmpty())) {

                        int index = lstPlayList.getSelectedIndex();
                        File fd = (File) listModel.elementAt(index);

                        controller.open(fd);
                        controller.stop();
                        controller.play();
                        paused = false;
                        playing = true;
                        slider.setEnabled(true);

                        // try {
                        // MpegInfo mi = new MpegInfo(((File)
                        // listModel.elementAt(index)).getPath());
                        // System.out.println("Informations diverses ");
                        // System.out.println("Artiste : " + mi.getArtist());
                        // System.out.println("Album : " + mi.getAlbum());
                        // System.out.println("Année : " + mi.getYear());
                        // System.out.println("Titre : " + mi.getTitle());
                        // }
                        // catch (ID3Exception ex) {}
                        // catch (IOException ex) {}
                        // catch (JavaLayerException ex) {}
                    }
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
            }

            // Pause d'une chanson à partir du bouton de la barre de status
            else if (e.getSource() == buttonPause) {
                try {
                    if (paused == true) {
                        controller.resume();
                        paused = false;
                    }
                    else {
                        controller.pause();
                        paused = true;
                    }
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
            }

            // Stop d'une chanson à partir de la barre de status
            else if (e.getSource() == buttonStop) {
                try {
                    controller.stop();
                    model.setValue(0);
                    paused = false;
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
                playing = false;
                slider.setValue(0);
                slider.setEnabled(false);
                time.setText("00:00 / " + millisec_to_time(milliseconds));
            }

            // Suppression de tous les elements de la playlist à partir
            // du menu
            else if (c.equals("clear")) {
                listModel.removeAllElements();
            }

            // Chargement d'une playlist à partir du menu
            else if (c.equals("load")) {
                loadPlaylist();
            }

            // Enregistrement d'une playlist à partir du menu
            else if (c.equals("save")) {
                savePlaylist();
            }

            // Next : passage à la chanson suivante à partir du bouton de la
            // barre
            // de status
            else if (e.getSource() == buttonNext) {
                getNext();
            }

            // Prev : passage à la chanson precedente à partir du bouton de la
            // barre
            // de status
            else if (e.getSource() == buttonPrev) {
                getPrev();
            }

            // Pour l'instant rien
            else if (c.equals("fileinfo")) {}

            // Play : joue une chanson à partir du menu du popup
            // Attention à rejoindre avec le play normal
            else if (c.equals("playitem")) {
                try {
                    if (listModel.size() != 0 && !(lstPlayList.isSelectionEmpty())) {
                        int index = lstPlayList.getSelectedIndex();
                        controller.open((File) listModel.elementAt(index));
                        controller.play();
                    }
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
                playing = true;
                slider.setEnabled(true);

            }

            // Crop : ne garde dans la playlist que la selection courante
            // à partir du menu u popup
            else if (c.equals("cropitem")) {
                if (listModel.size() != 0 && !(lstPlayList.isSelectionEmpty())) {
                    int index = lstPlayList.getSelectedIndex();
                    File f = (File) listModel.getElementAt(index);
                    listModel.removeAllElements();
                    listModel.addElement(f);
                }
            }

            // Suppression d'un fichier à partir du popup
            // Attention a rejoindre av ce la suppression normale
            else if (c.equals("removeitem")) {
                if (listModel.size() != 0 && !(lstPlayList.isSelectionEmpty())) {
                    int index = lstPlayList.getSelectedIndex();
                    listModel.remove(index);
                }
            }

            // Permet de lancer le menu de configuration de la playlist
            else if (c.equals("config")) {
                if (toolFrame.isShowing())
                    toolFrame.setVisible(false);
                else
                    toolFrame.setVisible(true);
            }

            // Permet de lancer le Manager pour la PlayList
            else if (c.equals("manager")) {
                if (plm != null) {
                    plm.setSupportedFileFormats(controller.getSupportedFileTypeExtensions());
                }

                if (plm.isShowing())
                    plm.setVisible(false);
                else
                    plm.setVisible(true);
            }

            // Permet de savoir s'il faut inclure les sous repertoires à
            // l'inclusion
            // d'un repertoire à la playlist
            else if (e.getSource() == cb) {
                recursion = cb.isSelected();
            }
        }
    }

    /**
     * Permet l'affichage des ToolTip sur les figures
     * 
     * @return la chaine décrivant la figure et qui va etre affichée
     */
    public String getToolTipText(MouseEvent e) {
        // System.out.println("on est dans le tooltip");
        int index = lstPlayList.locationToIndex(e.getPoint());
        File f = (File) listModel.getElementAt(index);
        if (f != null) return f.getName();
        return null;
    }

    /** ************************************************************************** */
    /** ************************************************************************** */
    /** ************************************************************************** */

    /** Classe dédiée au Popup de la JList */
    class PopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        /**
         * Fonction qui initialise le PopUp lors de son affichage i.e. qui
         * active ou désactive certains menus selon le contexte
         */
        public void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int index = lstPlayList.getSelectedIndex();
                if (index != -1) {
                    /***********************************************************
                     * System.out.println("coor : " + e.getX() + ", " +
                     * e.getY()); System.out.println("Nom du fichier : " +
                     * ((List) e.getComponent()).getSelectedItem());
                     **********************************************************/

                    popup.show(e.getComponent(), e.getX(), e.getY());
                    popup.revalidate();
                }
            }
        }
    }

    /***************************************************************************
     * Ensemble des fonctions à implementer lors de l'utilisation de l'interface
     * MouseListener
     **************************************************************************/
    public void mousePressed(MouseEvent e) {
        popupListener.maybeShowPopup(e);
        int index = lstPlayList.locationToIndex(e.getPoint());
        lstPlayList.setSelectedIndex(index);
        indexSelected = index;
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == slider) {
            try {
                long skp = -1;
                synchronized (model) {
                    skp = (long) ((model.getValue() * 1.0f / modelscale * 1.0f) * byteslength);
                }
                controller.seek(skp);
            }
            catch (BasicPlayerException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void mouseExited(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int index = lstPlayList.locationToIndex(e.getPoint());
            if (index != -1) {
                try {
                    File fd = (File) listModel.elementAt(index);

                    controller.open(fd);
                    controller.play();
                    playing = true;
                    slider.setEnabled(true);
                }
                catch (BasicPlayerException el) {
                    el.printStackTrace();
                }
            }
        }
    }

    /**
     * opened
     * 
     * @param stream
     *        Object
     * @param properties
     *        Map
     */
    public String millisec_to_time(long time_ms) {
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
        return ( /* strhours + ":" + */strminutes + ":" + strseconds);
    }

    public void opened(Object stream, Map properties) {
        if (properties.containsKey("audio.length.bytes")) {
            byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
        }

        if (properties.containsKey("duration")) {
            milliseconds = (int) (((Long) properties.get("duration")).longValue()) / 1000;
        }

        // time.setText("00:00 /" + millisec_to_time(milliseconds));
    }

    public String getDuration() {
        return null;
    }

    /**
     * Progress. Callback from the BasicPlayer.
     * 
     * @param bytesread
     *        int
     * @param microseconds
     *        long
     * @param pcmdata
     *        byte[]
     * @param properties
     *        Map
     */
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {

        // long elapsedMilliseconds = (long) (microseconds / 1000);
        float progress = bytesread * 1.0f / this.byteslength * 1.0f;
        long progressMilliseconds = (long) (progress * milliseconds);
        time.setText(millisec_to_time(progressMilliseconds) + " / " + millisec_to_time(milliseconds));
        model.setValue((int) (progress * modelscale));

        // If we are in single song mode, we dont need to test for playing the
        // next song.
        if (isSingleSongMode()) return;

        // If we are near to end of song (1.5 s)
        if (Math.abs(progressMilliseconds - milliseconds) <= 1000) {
            // if
            // (millisec_to_time(progressMilliseconds).equals(millisec_to_time(
            // milliseconds))) {
            // Let's play next song
            System.out.println("We do play next song ! diff = " + Math.abs(progressMilliseconds - milliseconds));
            getNext();
        }
    }

    public int getNextIndex(int index) {
        int taille = listModel.size();
        if (index < taille) return index + 1;

        return 0;
    }

    /**
     * stateUpdated
     * 
     * @param event
     *        BasicPlayerEvent
     */
    public void stateUpdated(BasicPlayerEvent event) {}

    /**
     * focusGained
     * 
     * @param e
     *        FocusEvent
     */
    public void focusGained(FocusEvent e) {}

    /**
     * focusLost
     * 
     * @param e
     *        FocusEvent
     */
    public void focusLost(FocusEvent e) {}

    /**
     * mouseDragged
     * 
     * @param e
     *        MouseEvent
     */
    public void mouseDragged(MouseEvent e) {
        int index = lstPlayList.locationToIndex(e.getPoint());
        if (currentSelection != index && indexSelected != index) {
            currentSelection = index;
            // System.out.println("on inverse : " +
            // (listModel.getElementAt(indexSelected)).toString());
            // System.out.println("avec : " + listModel.getElementAt(index));

            if (index > indexSelected) {
                File f = (File) listModel.getElementAt(index - 1);
                listModel.removeElementAt(index - 1);
                listModel.insertElementAt(f, index);
                lstPlayList.setSelectedIndex(index);
                indexSelected = index;
            }
            else {
                File f = (File) listModel.getElementAt(index + 1);
                listModel.removeElementAt(index + 1);
                listModel.insertElementAt(f, index);
                lstPlayList.setSelectedIndex(index);
                indexSelected = index;
            }
        }
    }

    /**
     * mouseMoved
     * 
     * @param e
     *        MouseEvent
     */
    public void mouseMoved(MouseEvent e) {}

    /**
     * getPlugin
     * 
     * @return BasicPlayerListener
     */
    public BasicPlayerListener getPlugin() {
        return this;
    }

    /**
     * dragEnter
     * 
     * @param e
     *        DropTargetDragEvent
     */
    public void dragEnter(DropTargetDragEvent e) {
        if (isDragOk(e) == false) {
            e.rejectDrag();
            return;
        }
    }

    /**
     * dragOver
     * 
     * @param e
     *        DropTargetDragEvent
     */
    public void dragOver(DropTargetDragEvent e) {
        if (isDragOk(e) == false) {
            e.rejectDrag();
            return;
        }
    }

    /**
     * dragExit
     * 
     * @param e
     *        DropTargetEvent
     */
    public void dragExit(DropTargetEvent e) {}

    /**
     * dropActionChanged
     * 
     * @param e
     *        DropTargetDragEvent
     */
    public void dropActionChanged(DropTargetDragEvent e) {
        if (isDragOk(e) == false) {
            e.rejectDrag();
            return;
        }
    }

    /**
     * ajoutes les fichiers à la playlist lors du drop nous pouvons ajouter un
     * ou plusieurs fichier venant de l'exterieur ainsi que des playlist
     * 
     * @param e
     *        DropTargetDropEvent
     */
    public void drop(DropTargetDropEvent e) {
        // Verification DataFlavor
        DataFlavor[] dfs = e.getCurrentDataFlavors();
        DataFlavor tdf = null;
        for (int i = 0; i < dfs.length; i++) {
            if (DataFlavor.javaFileListFlavor.equals(dfs[i]) || DataFlavor.stringFlavor.equals(dfs[i])) {
                tdf = dfs[i];
                break;
            }
        }
        /** si on a trouve une bonne dataFlavor */
        if (tdf != null) {
            /** et que l'on souhite faire une copie */
            if ((e.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
                /** le drop est accepte */
                e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            }
            else {
                return;
            }
            try {
                Transferable t = e.getTransferable();
                Object data = t.getTransferData(tdf);

                if (data instanceof java.util.List) {
                    java.util.List al = (java.util.List) data;

                    if (al.size() > 0) {
                        File file = null;

                        /**
                         * ajout des données recues dans la liste graphique et
                         * réelle
                         */
                        ListIterator li = al.listIterator();
                        while (li.hasNext()) {
                            file = (File) li.next();
                            if (file.isFile()) {
                                /** s'il s'agit des chansons * */
                                if (file != null && controller.isFileSupported(file)) {
                                    listModel.addElement(file);
                                }
                            }
                            else if (file.isDirectory()) {
                                parcoursRecursif(file, isIncludeSubFolderForDragAndDrop(), 0);
                            }
                        }
                    }
                }
                else if (data instanceof java.lang.String) {
                    parseData((String) data);
                }
            }
            catch (IOException ioe) {
                e.dropComplete(false);
                return;
            }
            catch (UnsupportedFlavorException ufe) {
                e.dropComplete(false);
                return;
            }
            catch (Exception ex) {
                e.dropComplete(false);
                return;
            }
            e.dropComplete(true);
        }
    }

    public void parseData(String data) {
        StringTokenizer st = new StringTokenizer(data, "\n");
        while (st.hasMoreTokens()) {
            listModel.addElement(new File(st.nextToken()));
        }
    }

    private boolean isDragOk(DropTargetDragEvent e) {
        // verification DataFlavor
        DataFlavor[] dfs = e.getCurrentDataFlavors();
        DataFlavor tdf = null;
        for (int i = 0; i < dfs.length; i++) {
            /**
             * si on essaie d'ajouter un fichier ou du texte, cad un path pour
             * un fichier mp3
             */
            if (DataFlavor.javaFileListFlavor.equals(dfs[i]) || DataFlavor.stringFlavor.equals(dfs[i])) {
                tdf = dfs[i];
                break;
            }
        }
        if (tdf != null) {
            if ((e.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
                return true;
            }

            return false;
        }

        return false;
    }
}
