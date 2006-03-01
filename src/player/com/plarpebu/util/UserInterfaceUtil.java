package com.plarpebu.util;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class UserInterfaceUtil {
    /**
     * Center any frame on the screen
     * 
     * @param frame
     */
    public static void centerFrame(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }
}
