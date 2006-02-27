package plugins.karaoke.cdg.basicplayer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Michel Buffa (buffa@unice.fr)
 * @version $Id
 */

public class ChooseFullScreenModeDIalog extends JDialog {
    JPanel jPanel1 = new JPanel();

    JButton jButtonOk = new JButton();

    JScrollPane jScrollPane1 = new JScrollPane();

    JTable dmList = new JTable();

    public static final int INDEX_WIDTH = 0;

    public static final int INDEX_HEIGHT = 1;

    public static final int INDEX_BITDEPTH = 2;

    public static final int INDEX_REFRESHRATE = 3;

    public static final int[] COLUMN_WIDTHS = new int[] { 100, 100, 100, 100 };

    public static final String[] COLUMN_NAMES = new String[] { "Width", "Height", "Bit Depth", "Refresh Rate" };

    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

    GraphicsDevice[] devices = env.getScreenDevices();

    DisplayModeModel model = new DisplayModeModel(devices[0].getDisplayModes());

    JFrame parentFrame;

    public ChooseFullScreenModeDIalog(JFrame parent, boolean modal) {
        super(parent, modal);
        parentFrame = parent;
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // List of display modes
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            TableColumn col = new TableColumn(i, COLUMN_WIDTHS[i]);
            col.setIdentifier(COLUMN_NAMES[i]);
            col.setHeaderValue(COLUMN_NAMES[i]);
            dmList.addColumn(col);
        }
        dmList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dmList.setModel(model);
        setSize(200, 300);
    }

    // The Dialog will appear centered
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
        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        super.setVisible(flag);
    }

    private void jbInit() throws Exception {
        this.setModal(true);
        this.setTitle("Choose fullscreen mode");
        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new ChooseFullScreenModeDIalog_jButtonOk_actionAdapter(this));
        this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(jButtonOk, null);
        this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(dmList, null);
    }

    public DisplayMode getSelectedDisplayMode() {
        DisplayMode dm = null;
        int index = dmList.getSelectionModel().getAnchorSelectionIndex();
        if (index >= 0) {
            DisplayModeModel model = (DisplayModeModel) dmList.getModel();
            dm = model.getDisplayMode(index);
        }
        return dm;
    }

    class DisplayModeModel extends DefaultTableModel {
        private DisplayMode[] modes;

        public DisplayModeModel(DisplayMode[] modes) {
            this.modes = modes;
        }

        public DisplayMode getDisplayMode(int r) {
            return modes[r];
        }

        public String getColumnName(int c) {
            return ChooseFullScreenModeDIalog.COLUMN_NAMES[c];
        }

        public int getColumnCount() {
            return ChooseFullScreenModeDIalog.COLUMN_WIDTHS.length;
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }

        public int getRowCount() {
            if (modes == null) {
                return 0;
            }
            return modes.length;
        }

        public Object getValueAt(int rowIndex, int colIndex) {
            DisplayMode dm = modes[rowIndex];
            switch (colIndex) {
            case ChooseFullScreenModeDIalog.INDEX_WIDTH:
                return Integer.toString(dm.getWidth());
            case ChooseFullScreenModeDIalog.INDEX_HEIGHT:
                return Integer.toString(dm.getHeight());
            case ChooseFullScreenModeDIalog.INDEX_BITDEPTH: {
                int bitDepth = dm.getBitDepth();
                String ret;
                if (bitDepth == DisplayMode.BIT_DEPTH_MULTI) {
                    ret = "Multi";
                }
                else {
                    ret = Integer.toString(bitDepth);
                }
                return ret;
            }
            case ChooseFullScreenModeDIalog.INDEX_REFRESHRATE: {
                int refreshRate = dm.getRefreshRate();
                String ret;
                if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
                    ret = "Unknown";
                }
                else {
                    ret = Integer.toString(refreshRate);
                }
                return ret;
            }
            }
            throw new ArrayIndexOutOfBoundsException("Invalid column value");
        }

    }

    void jButtonCancel_actionPerformed(ActionEvent e) {

    }

    void jButtonOk_actionPerformed(ActionEvent e) {
        setVisible(false);
    }

}

class ChooseFullScreenModeDIalog_jButtonOk_actionAdapter implements java.awt.event.ActionListener {
    ChooseFullScreenModeDIalog adaptee;

    ChooseFullScreenModeDIalog_jButtonOk_actionAdapter(ChooseFullScreenModeDIalog adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonOk_actionPerformed(e);
    }
}