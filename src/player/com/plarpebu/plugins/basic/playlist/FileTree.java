package com.plarpebu.plugins.basic.playlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class FileTree extends JPanel implements ActionListener {

    public static final ImageIcon ICON_COMPUTER = new ImageIcon(FileTree.class
    .getResource("icones/computer.png"));

    public static final ImageIcon ICON_DISK = new ImageIcon(FileTree.class
    .getResource("icones/disk.gif"));

    public static final ImageIcon ICON_FOLDER = new ImageIcon(FileTree.class
    .getResource("icones/folder.gif"));

    public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon(FileTree.class
    .getResource("icones/expandfolder.gif"));

    protected JTree m_tree;

    protected DefaultTreeModel m_model;

    protected DefaultMutableTreeNode currentNode;

    protected static boolean recursion = false;

    private DefaultListModel track = null;

    private JList jlist = null;

    private JCheckBox cb = null;

    private JComboBox comboNode = null;

    private File[] roots;

    private DefaultMutableTreeNode top;

    private DefaultMutableTreeNode node;

    private JButton add = null;

    private JButton clear = null;

    private JComboBox comboFav = null;

    private String path = "";

    private MyFileFilter treefilter = null;

    private JComboBox comboFilter = null;

    protected String[] supportedFileTypeExtensions = {};

    public FileTree(JList jl) {
        setLayout(new GridLayout());
        jlist = jl;
        track = (DefaultListModel) jl.getModel();
        treefilter = new MyFileFilter();

        top = new DefaultMutableTreeNode(new IconData(ICON_COMPUTER, null, "Computer"));

        roots = File.listRoots();
        comboNode = new JComboBox(roots);
        comboNode.setMaximumSize(new Dimension(500, 20));
        comboNode.addActionListener(this);
        node = new DefaultMutableTreeNode(new IconData(ICON_DISK, null, new FileNode(roots[0])));
        top.add(node);
        node.add(new DefaultMutableTreeNode(new Boolean(true)));

        m_model = new DefaultTreeModel(top);
        m_tree = new JTree(m_model);
        m_tree.putClientProperty("JTree.lineStyle", "Angled");

        TreeCellRenderer renderer = new IconCellRenderer();
        m_tree.setCellRenderer(renderer);

        m_tree.addTreeExpansionListener(new DirExpansionListener());

        m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_tree.setShowsRootHandles(true);
        m_tree.setEditable(false);

        JScrollPane s = new JScrollPane();
        s.getViewport().add(m_tree);

        Box bb = Box.createVerticalBox();
        bb.setBorder(BorderFactory.createTitledBorder("Computer"));
        bb.setPreferredSize(new Dimension(230, 500));

        cb = new JCheckBox("Include Subfolders", false);
        cb.setAlignmentX(LEFT_ALIGNMENT);
        cb.addActionListener(this);

        // Panel contenant les favoris
        Box Boxfavoris = Box.createVerticalBox();
        Boxfavoris.setBorder(BorderFactory.createTitledBorder("Favorite Folders"));
        comboFav = new JComboBox();
        comboFav.setMaximumSize(new Dimension(500, 20));
        comboFav.addActionListener(this);
        loadFavorites();

        add = new JButton("Add");
        add.addActionListener(this);

        clear = new JButton("Clear");
        clear.addActionListener(this);

        Box buttons = Box.createHorizontalBox();
        buttons.add(add);
        buttons.add(clear);
        Boxfavoris.add(buttons);
        Boxfavoris.add(comboFav);

        Box filter = Box.createHorizontalBox();
        filter.setBorder(BorderFactory.createTitledBorder("Filter Options"));
        String[] filterN = { "All Files", ".mp3", ".cdg", ".xml", ".m3u", ".zip" };
        comboFilter = new JComboBox(filterN);
        comboFilter.setMaximumSize(new Dimension(500, 20));
        comboFilter.addActionListener(this);
        filter.add(comboFilter);

        bb.add(comboNode);
        bb.add(s);
        bb.add(Boxfavoris);
        bb.add(cb);
        bb.add(filter);

        this.add(bb);
    }

    /**
     * Permet de gerer la plupart de evenement relatifs au composants
     * 
     * @param e
     *        ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cb) {
            recursion = cb.isSelected();
            FileNode fnode = getFileNode(currentNode);
            if (fnode != null) {
                fnode.liste.removeAllElements();
                fnode.filefilter = treefilter;
                fnode.getFiles(fnode.m_file, 0);
                setList(fnode.liste);
            }
        }
        else if (e.getSource() == comboNode) {
            int index = comboNode.getSelectedIndex();
            top.removeAllChildren();
            node = new DefaultMutableTreeNode(new IconData(ICON_DISK, null, new FileNode(roots[index])));
            top.add(node);
            node.add(new DefaultMutableTreeNode(new Boolean(true)));
            currentNode = node;
            m_model.reload();
        }

        else if (e.getSource() == comboFav) {
            int index = comboFav.getSelectedIndex();
            if (index != -1) {
                File f = new File((String) comboFav.getItemAt(index));
                top.removeAllChildren();
                node = new DefaultMutableTreeNode(new IconData(ICON_DISK, null, new FileNode(f)));
                top.add(node);
                node.add(new DefaultMutableTreeNode(new Boolean(true)));
                m_model.reload();
                currentNode = node;
                FileNode fn = getFileNode(node);
                fn.getFiles(f, 0);
                setList(fn.liste);
            }
        }

        else if (e.getSource() == add) {
            if (!path.equals("")) {
                int taille = comboFav.getItemCount();
                boolean res = false;
                for (int i = 0; i < taille; i++)
                    if (comboFav.getItemAt(i).equals(path)) {
                        res = true;
                    }
                if (!res) {
                    comboFav.addItem(path);
                    saveFavorites();
                }
            }
        }
        else if (e.getSource() == clear) {
            Object o = comboFav.getSelectedItem();
            if (o != null) comboFav.removeItem(o);
            saveFavorites();
        }
        else if (e.getSource() == comboFilter) {
            String s = (String) comboFilter.getSelectedItem();
            FileNode fn = getFileNode(currentNode);
            fn.liste.removeAllElements();
            if (s.equals("mp3")) {
                treefilter.setExtension(s);
            }
            else if (s.equals("cdg")) {
                treefilter.setExtension(s);
            }
            else {
                treefilter.setExtension("");
            }
            fn.filefilter = treefilter;
            fn.getFiles(fn.m_file, 0);
            setList(fn.liste);
        }

    }

    public void setSupportedFileFormats(String[] exts) {
        supportedFileTypeExtensions = new String[exts.length];
        System.arraycopy(exts, 0, supportedFileTypeExtensions, 0, exts.length);
    }

    /**
     * Checks if the file is in a supported format
     * 
     * @param file
     * @return true if file is supported, false otherwise
     */
    public boolean isFileSupported(File file) {
        return isFileSupported(file.getName());
    }

    /**
     * Checks if the file is in a supported format
     * 
     * @param filename
     * @return true if file is supported, false otherwise
     */
    public boolean isFileSupported(String filename) {
        for (int i = 0; i < supportedFileTypeExtensions.length; i++) {
            if (filename.toLowerCase().endsWith(supportedFileTypeExtensions[i])) return true;
        }
        return false;
    }

    /**
     * Permet de mettre à jour la tracklist
     * 
     * @param l
     *        La liste permettant de mettre à jour la tracklist
     */
    protected void setList(DefaultListModel l) {
        track.removeAllElements();
        for (int i = 0; i < l.size(); i++) {
            File file = (File) l.elementAt(i);
            if (isFileSupported(file)) {
                track.addElement(file);
            }
        }
        if (track.size() > 0) {
            jlist.setSelectedIndex(0);
        }
    }

    /**
     * Permet de recuperer un noeud à partir du path
     * 
     * @param path
     *        le path dans l'arbre
     * @return dftm le noeud correspondant au path
     */
    protected DefaultMutableTreeNode getTreeNode(TreePath path) {
        return (DefaultMutableTreeNode) (path.getLastPathComponent());
    }

    /**
     * Permet de recuperer un FileNode à partir d'un noeud
     * 
     * @param node
     *        un noeud
     * @return fn le FileNode au noeud
     */
    protected FileNode getFileNode(DefaultMutableTreeNode node) {
        if (node == null) {
            return null;
        }
        Object obj = node.getUserObject();
        if (obj instanceof IconData) {
            obj = ((IconData) obj).getObject();
        }
        if (obj instanceof FileNode) {
            return (FileNode) obj;
        }

        return null;
    }

    /**
     * Permet de sauvegarder les favoris (cad les path des noeuds favoris) dans
     * un fichier texte de l'utilisateur
     */
    public void saveFavorites() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("favorites.txt")));

            int taille = comboFav.getItemCount();
            for (int i = 0; i < taille; i++) {
                bw.write((String) comboFav.getItemAt(i));
                bw.newLine();
            }
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Permet de charger à partir d'un fichier texte les favoris (cad les path
     * des noeuds) de l'utilisateur
     */
    public void loadFavorites() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("favorites.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                comboFav.addItem(line);
            }
            br.close();
        }
        catch (IOException ex) {
            System.out.println("Aucun favoris enregistrés");
            ;
        }
    }

    /***************************************************************************
     * Classe qui assure l'expansion de chaque noeud et la mise à jour * de
     * l'arbre à travers un thread *
     **************************************************************************/

    class DirExpansionListener implements TreeExpansionListener {

        public void treeExpanded(TreeExpansionEvent event) {
            final DefaultMutableTreeNode node = getTreeNode(event.getPath());
            final FileNode fnode = getFileNode(node);

            Thread runner = new Thread() {
                public void run() {
                    if (fnode != null && fnode.expand(node)) {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                m_model.reload(node);
                                fnode.filefilter = treefilter;
                                fnode.getFiles(fnode.m_file, 0);
                                setList(fnode.liste);
                                currentNode = node;
                                path = fnode.getFile().getAbsolutePath();
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                }
            };
            runner.start();
        }

        public void treeCollapsed(TreeExpansionEvent event) {}
    }
}

/*******************************************************************************
 * Classe qui assure certaines actions lors de la selection et de * l'expansion
 * d'un noeud (ici on met à jour le path et le neoud courant) *
 ******************************************************************************/
class IconCellRenderer extends JLabel implements TreeCellRenderer {

    protected Color m_textSelectionColor;

    protected Color m_textNonSelectionColor;

    protected Color m_bkSelectionColor;

    protected Color m_bkNonSelectionColor;

    protected Color m_borderSelectionColor;

    protected boolean m_selected;

    public IconCellRenderer() {
        super();
        m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
        m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
        m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
        m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
        m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
        setOpaque(false);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
    boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();
        setText(obj.toString());

        if (obj instanceof Boolean) {
            setText("Retrieving data...");
        }

        if (obj instanceof IconData) {
            IconData idata = (IconData) obj;
            if (expanded) {
                setIcon(idata.getExpandedIcon());
            }
            else {
                setIcon(idata.getIcon());
            }
        }
        else {
            setIcon(null);
        }
        setFont(tree.getFont());
        setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
        setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
        m_selected = sel;
        return this;
    }

    public void paintComponent(Graphics g) {
        Color bColor = getBackground();
        Icon icon = getIcon();

        g.setColor(bColor);
        int offset = 0;
        if (icon != null && getText() != null) {
            offset = (icon.getIconWidth() + getIconTextGap());
        }
        g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);

        if (m_selected) {
            g.setColor(m_borderSelectionColor);
            g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
        }
        super.paintComponent(g);
    }
}

/*******************************************************************************
 * Classe qui assure certaines actions lors de la selection et de * l'expansion
 * d'un noeud (ici on met à jour le path et le neoud courant) *
 ******************************************************************************/
class IconData {
    protected Icon m_icon;

    protected Icon m_expandedIcon;

    protected Object m_data;

    public IconData(Icon icon, Object data) {
        m_icon = icon;
        m_expandedIcon = null;
        m_data = data;
    }

    public IconData(Icon icon, Icon expandedIcon, Object data) {
        m_icon = icon;
        m_expandedIcon = expandedIcon;
        m_data = data;
    }

    public Icon getIcon() {
        return m_icon;
    }

    public Icon getExpandedIcon() {
        return m_expandedIcon != null ? m_expandedIcon : m_icon;
    }

    public Object getObject() {
        return m_data;
    }

    public String toString() {
        return m_data.toString();
    }
}

/*******************************************************************************
 * Classe qui assure certaines actions lors de la selection et de * l'expansion
 * d'un noeud (ici on met à jour le path et le neoud courant) *
 ******************************************************************************/
class FileNode {

    protected File m_file;

    protected DefaultListModel liste;

    protected MyFileFilter filefilter = null;

    public FileNode(File file) {
        m_file = file;
        liste = new DefaultListModel();
        filefilter = null;
    }

    public File getFile() {
        return m_file;
    }

    public String toString() {
        return m_file.getName().length() > 0 ? m_file.getName() : m_file.getPath();
    }

    public boolean expand(DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent.getFirstChild();

        if (flag == null) {
            return false;
        }

        parent.removeAllChildren();

        File[] files = listFiles(null);
        liste.removeAllElements();

        if (files == null) {
            return true;
        }

        Vector v = new Vector();

        for (int k = 0; k < files.length; k++) {
            File f = files[k];
            if (!(f.isDirectory())) {
                continue;
            }

            FileNode newNode = new FileNode(f);

            boolean isAdded = false;
            for (int i = 0; i < v.size(); i++) {
                FileNode nd = (FileNode) v.elementAt(i);
                if (newNode.compareTo(nd) < 0) {
                    v.insertElementAt(newNode, i);
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) {
                v.addElement(newNode);
            }
        }

        for (int i = 0; i < v.size(); i++) {
            FileNode nd = (FileNode) v.elementAt(i);
            IconData idata = new IconData(FileTree.ICON_FOLDER, FileTree.ICON_EXPANDEDFOLDER, nd);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
            parent.add(node);

            node.add(new DefaultMutableTreeNode(new Boolean(true)));
        }

        return true;
    }

    public void getFiles(File f, int depth) {
        if (depth > PlayListPlugin.MAXDEPTH) {
            System.out.println("Stopping recursion, MAXDEPTH = " + PlayListPlugin.MAXDEPTH + " reached");
            return;
        }

        File[] files;
        if (filefilter == null || filefilter.getExtension().equals("")) {
            files = f.listFiles();
        }
        else {
            files = f.listFiles(filefilter);
        }
        if (files != null) {
            for (int k = 0; k < files.length; k++) {
                if (files[k].isFile()) {
                    liste.addElement(files[k]);
                }
                else if (files[k].isDirectory() && FileTree.recursion) {
                    if (!(files[k].getName().equals("System Volume Information")) && true) {
                        getFiles(files[k], depth + 1);
                    }
                }
            }
        }
    }

    public boolean hasSubDirs() {
        File[] files = listFiles(filefilter);
        if (files == null) {
            return false;
        }
        for (int k = 0; k < files.length; k++) {
            if (files[k].isDirectory()) {
                return true;
            }
        }
        return false;
    }

    public int compareTo(FileNode toCompare) {
        return m_file.getName().compareToIgnoreCase(toCompare.m_file.getName());
    }

    protected File[] listFiles(MyFileFilter filefilter) {
        if (!m_file.isDirectory()) {
            return null;
        }
        try {
            if (filefilter == null || filefilter.getExtension().equals("")) return m_file.listFiles();

            return m_file.listFiles(filefilter);
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error reading directory " + m_file.getAbsolutePath(), "Warning",
            JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }
}
