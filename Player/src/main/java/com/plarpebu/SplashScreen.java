package com.plarpebu;

// SplashScreen.java
// A simple application to show a title screen in the center of the screen
// for the amount of time given in the constructor.  This class includes
// a sample main() method to test the splash screen, but it's meant for use
// with other applications.
//

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.Border;

import fr.unice.plugin.PluginManagerEvent;
import fr.unice.plugin.PluginManagerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashScreen extends JWindow implements PluginManagerListener {

  private final static Logger logger = LoggerFactory.getLogger(SplashScreen.class);

  ImageIcon icon1 = new ImageIcon(SplashScreen.class.getResource("/icons/splash.jpg"));

  JPanel jPanel1 = new JPanel();

  JLabel jLabel1 = new JLabel();

  Border border1;

  JLabel jLabel2 = new JLabel();

  BorderLayout borderLayout1 = new BorderLayout();

  public void showSplash() {
    // never disappear
    showSplash(0);
  }

  // A simple little method to show a title screen in the center
  // of the screen for the amount of time given in the constructor
  public void showSplash(int duration) {
    // JPanel content = (JPanel)getContentPane();
    // content.setBackground(Color.white);

    // Set the window's bounds, centering the window
    int width = 500;
    int height = 330;
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (screen.width - width) / 2;
    int y = (screen.height - height) / 2;
    setBounds(x, y, width, height);
    pack();

    // Build the splash screen
    // JLabel label = new JLabel(new
    // ImageIcon(player.test.SplashScreen.class.getResource("oreilly.gif")));
    // JLabel copyrt = new JLabel
    // ("Copyright 2002, O'Reilly & Associates", JLabel.CENTER);
    /*
     * copyrt.setFont(new Font("Sans-Serif", Font.BOLD, 12)); content.add(label,
		 * BorderLayout.CENTER); content.add(copyrt, BorderLayout.SOUTH); Color oraRed = new
		 * Color(156, 20, 20, 255); content.setBorder(BorderFactory.createLineBorder(oraRed, 10));
		 */
    // Display it
    setVisible(true);

    if(duration != 0) {
      // Wait a little while, maybe while loading resources
      try {
        Thread.sleep(duration);
      } catch(Exception e) {
        logger.warn(e.getMessage(), e);
      }

      setVisible(false);
    }
  }

  public void showSplashAndExit(int duration) {
    showSplash(duration);
    // System.exit(0);
    close();
  }

  public void close() {
    setVisible(false);
    dispose();
  }

  public static void main(String[] args) {
    // Throw a nice little title page up on the screen first
    SplashScreen splash = new SplashScreen();
    // Normally, we'd call splash.showSplash() and get on with the program.
    // But, since this is only a test...
    splash.showSplashAndExit(10000);
  }

  public SplashScreen() {
    try {
      jbInit();
    } catch(Exception e) {
      logger.warn(e.getMessage(), e);
    }
  }

  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(new Color(156, 20, 20, 255), 2);
    jLabel1.setBackground(Color.white);
    jLabel1.setIcon(icon1);
    jPanel1.setLayout(borderLayout1);
    jPanel1.setBorder(border1);
    jLabel2.setFont(new java.awt.Font("SansSerif", 1, 12));
    jLabel2.setText("Looking for plugins...");
    this.getContentPane().setBackground(Color.white);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jLabel1, BorderLayout.CENTER);
    jPanel1.add(jLabel2, BorderLayout.SOUTH);
  }

  @Override
  public void pluginLoaded(PluginManagerEvent e) {
    jLabel2.setText("loaded : " + e.getPlugin().getName());
  }
}
