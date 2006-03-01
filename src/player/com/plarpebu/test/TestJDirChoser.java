package com.plarpebu.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.l2fprod.common.swing.JDirectoryChooser;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

/**
 * ChooseDirectory. <br>
 * A simple example showing how to use the JDirectoryChooser.
 */
public class TestJDirChoser implements ActionListener {

    public TestJDirChoser() {
        JDirectoryChooser chooser = new JDirectoryChooser();

        JTextArea accessory = new JTextArea("Select directory. Use the 'Control' key"
        + " to select several directories");
        accessory.setLineWrap(true);
        accessory.setWrapStyleWord(true);
        accessory.setEditable(false);
        accessory.setOpaque(false);
        accessory.setFont(UIManager.getFont("Tree.font"));

        chooser.setAccessory(accessory);

        chooser.setMultiSelectionEnabled(true);
        chooser.addActionListener(this);

        int choice = chooser.showOpenDialog(null);
        if (choice == JDirectoryChooser.APPROVE_OPTION) {
            String message = "You selected:";
            File[] selectedFiles = chooser.getSelectedFiles();
            for (int i = 0, c = selectedFiles.length; i < c; i++) {
                message += "\n" + selectedFiles[i];
            }
            JOptionPane.showMessageDialog(null, message);
        }
        else {
            JOptionPane.showMessageDialog(null, "You clicked 'Cancel'");
        }

        /*
         * JFrame f = new JFrame(); f.getContentPane().add(chooser,
         * BorderLayout.CENTER); f.pack(); f.setVisible(true);
         */
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new TestJDirChoser();
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("Action performed");
    }

}
