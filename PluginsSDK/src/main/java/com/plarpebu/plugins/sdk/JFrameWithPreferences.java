package com.plarpebu.plugins.sdk;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import com.plarpebu.SkinMgr;
import com.plarpebu.common.PlarpebuUtil;

/**
 * Plugins can extends this class if they want to store preferences
 */
public abstract class JFrameWithPreferences extends JFrame implements SystemExitListener {

  // For preferences
  protected Properties preferences = null;

  private File preferencesDir = new File(PlarpebuUtil.applicationRootDirectory(), "preferences");

  private File preferenceFileName = new File(preferencesDir, "application.properties");

  private File defaultPreferencesFilename = new File(preferencesDir, "default.properties");

  public JFrameWithPreferences(String title) {
    super(title);
    SkinMgr.getInstance().addComponent(this);
  }

  // Preferences support. Each plugin can have a default preferences files
  // that will propose default values. This default file is called
  // defaultPlayListPlugin.properties for example, or
  // defaultVisuPlugin.properties...
  // Preferences file is overriden by user preference file. This file is
  // called
  // PlaylistPlugin.properties or Visu.properties.... (in fact
  // <PluginName>.properties,
  // and default value is default<PluginName>.properties.
  public void setPreferencesFileNames(String preferencesDir, String preferenceFileName, String defaultPreferencesFilename) {
    this.preferencesDir = new File(PlarpebuUtil.applicationRootDirectory(), preferencesDir);
    this.preferenceFileName = new File(this.preferencesDir, preferenceFileName);
    this.defaultPreferencesFilename = new File(this.preferencesDir, defaultPreferencesFilename);
  }

  /**
   * Read preferences
   */
  public void readPreferences() {
    Properties defaultProps = new Properties();
    InputStream in = null;

    // create and load default properties
    try {
      in = new FileInputStream(defaultPreferencesFilename);
      /*
       * System.out.println("On cherche la ressource " + "/"+defaultPreferencesFilename); in =
			 * pluginsSDK.JFrameWithPreferences.class.getResourceAsStream("/"+defaultPreferencesFilename);
			 */
      defaultProps.load(in);
      // create program properties with default
      preferences = new Properties(defaultProps);
      in.close();
    } catch(Exception e) {
      System.out.println("*****************************************");
      e.printStackTrace();
      try {
        System.out.println("No default preferences file found : " + defaultPreferencesFilename
          .getCanonicalPath() + " creating an empty one");
      } catch(IOException e1) {
        e1.printStackTrace();
      }
      createPreferencesFile(defaultPreferencesFilename);
    }

    try {
      // now load properties from last invocation
      in = new FileInputStream(preferenceFileName);
      preferences.load(in);
      in.close();
    } catch(Exception e) {
      System.out.println("No preferences file found : " + preferenceFileName + " Creating an empty one...");
      createPreferencesFile(preferenceFileName);
    }

    restorePosSizeAndVisibility();
  }

  private void restorePosSizeAndVisibility() throws NumberFormatException {
    if(preferences == null) {
      return;
    }

    // set pos and size of the Frame
    String stringValue = null;
    int x = 0, y = 0, width = 0, height = 0;

    // location
    if((stringValue = preferences.getProperty("x")) != null) {
      x = Integer.parseInt(stringValue);
    }
    if((stringValue = preferences.getProperty("y")) != null) {
      y = Integer.parseInt(stringValue);
    }

    setLocation(x, y);

    // size
    if((stringValue = preferences.getProperty("width")) != null) {
      width = Integer.parseInt(stringValue);
    }
    if((stringValue = preferences.getProperty("height")) != null) {
      height = Integer.parseInt(stringValue);
    }

    setSize(width, height);

    // restoreVisibility();
  }

  public void restoreVisibility() {
    // Visibility
    String stringValue = preferences.getProperty("isVisible");
    Boolean value = Boolean.valueOf(stringValue);
    setVisible(value.booleanValue());
  }

  private void createPreferencesFile(File filename) {
    try {
      File f = new File(PlarpebuUtil.applicationRootDirectory(), "preferences");
      if(!f.exists()) {
        System.out.println("The preferences dir does not exist, creating it !");
        f.mkdir();
      }

      FileOutputStream out = new FileOutputStream(new File(f, filename.getName()));
      out.flush();
      out.close();
    } catch(IOException ex) {
      System.out.println("Error creating empty preferences file : " + filename);
    }
  }

  public void savePreferences() {
    try {
      if(preferences != null) {

        // default behavior for all FramePlugin : save pos, size and
        // state (isVisible ?)
        preferences.setProperty("x", "" + getX());
        preferences.setProperty("y", "" + getY());
        preferences.setProperty("width", "" + getWidth());
        preferences.setProperty("height", "" + getHeight());
        preferences.setProperty("isVisible", "" + isVisible());

        FileOutputStream out = new FileOutputStream(preferenceFileName);
        preferences.store(out, "Preferences generated by JFrameWithPreferences.java");

        out.flush();
        out.close();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void exiting() {
    System.out.println(getClass().getName() + " is exiting, saving preferences");
    savePreferences();
  }

  public String colorToString(Color c) {
    String r = "" + c.getRed();
    String g = "" + c.getGreen();
    String b = "" + c.getBlue();
    String colorStringValue = r + "," + g + "," + b;
    return colorStringValue;
  }

  public Color StringToColor(String strColor) {
    StringTokenizer st = new StringTokenizer(strColor, ",");
    int r = Integer.parseInt(st.nextToken());
    int g = Integer.parseInt(st.nextToken());
    int b = Integer.parseInt(st.nextToken());
    // System.out.println("r = " + r + " g = " + g + " b = " + b);
    return new Color(r, g, b);
  }
}
