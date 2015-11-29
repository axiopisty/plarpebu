package com.plarpebu;

import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;
import com.plarpebu.common.PlarpebuUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Skin Manager
 */
public class SkinMgr {

  // Logger
  private static Logger logger = LoggerFactory.getLogger(SkinMgr.class);

  // Singelton instance
  private static SkinMgr instance = null;

  // Skins directory
  private static final String SKINDIR = "skins";
  private final File skinDir;

  // List of top level components to update when skin is changed
  private List<Component> components = new ArrayList<Component>();

  // List of skins
  private List<String> skins = new ArrayList<String>();

  /**
   * Get the singleton instance
   *
   * @return DOCUMENT ME!
   */
  public synchronized static SkinMgr getInstance() {
    if(instance == null) {
      instance = new SkinMgr();
    }

    return instance;
  }

  /**
   * Private Constructor
   */
  private SkinMgr() {
    // Get the skinLF skins (zip files)
    skinDir = new File(PlarpebuUtil.applicationRootDirectory(), SKINDIR);
    String[] fullSkinNames = skinDir.list(new SkinFilter());
    Arrays.sort(fullSkinNames);

    for(int i = 0; i < fullSkinNames.length; ++i) {
      skins.add(fullSkinNames[i].substring(0, fullSkinNames[i].lastIndexOf(".zip")));
    }

    logger.debug("Available skins: " + skins);
  }

  /**
   * Get the list of available skins
   *
   * @return DOCUMENT ME!
   */
  public List<String> getSkinNames() {
    return skins;
  }

  /**
   * Set the skin
   *
   * @param skinName DOCUMENT ME!
   */
  public void setSkin(String skinName) {
    try {
      // Try setting it as a skinlf skin
      File skin = new File(skinDir, skinName + ".zip");
      logger.info("Setting skin: " + skin.getName());
      SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(skin.toURI().toURL()));
      //This method is deprecated, but it doesnt seem to work properly without it.
      SkinLookAndFeel.enable();

      // Update the components
      updateComponents();
    } catch(Exception ex) {
      logger.warn("Could not set skin: " + ex.toString() + ": " + ex.getMessage(), ex);
    }
  }

  /**
   * Add top level component to update when skin changes
   *
   * @param component DOCUMENT ME!
   */
  public void addComponent(Component component) {
    components.add(component);
  }

  /**
   * Update components
   */
  private void updateComponents() {
    Iterator iter = components.iterator();

    while(iter.hasNext()) {
      Component c = (Component) iter.next();
      logger.debug("Updating component: " + c.getName());
      SwingUtilities.updateComponentTreeUI(c);
    }
  }

  /**
   * Filter for skin files
   */
  private class SkinFilter implements FilenameFilter {

    /**
     * DOCUMENT ME!
     *
     * @param dir  DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public boolean accept(File dir, String name) {
      if(name.toLowerCase().endsWith(".zip")) {
        return true;
      } else {
        return false;
      }
    }
  }
}
