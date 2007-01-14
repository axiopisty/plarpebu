package com.plarpebu;

import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

/**
 * Skin Manager
 */
public class SkinMgr
{
	// Logger
	private static Logger logger = Logger.getAnonymousLogger();

	// Singelton instance
	private static SkinMgr instance = null;

	// Skins directory
	private static final String SKINDIR = "skins";

	// List of top level components to update when skin is changed
	private List<Component> components = new ArrayList<Component>();

	// List of skins
	private List<String> skins = new ArrayList<String>();

	/**
	 * Get the singleton instance
	 * 
	 * @return DOCUMENT ME!
	 */
	public synchronized static SkinMgr getInstance()
	{
		if (instance == null)
			instance = new SkinMgr();

		return instance;
	}

	/**
	 * Private Constructor
	 */
	private SkinMgr()
	{
		//Set so that frame and dialogs are also decorated by SkinLF
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		// Get the skinLF skins (zip files)
		String[] fullSkinNames = (new File(SKINDIR)).list(new SkinFilter());

		for (int i = 0; i < fullSkinNames.length; ++i)
		{
			skins.add(fullSkinNames[i].substring(0, fullSkinNames[i].lastIndexOf(".zip")));
		}
		
		logger.info("Available skins: " + skins);
	}

	/**
	 * Get the list of available skins
	 * 
	 * @return DOCUMENT ME!
	 */
	public List<String> getSkinNames()
	{
		return skins;
	}

	/**
	 * Set the skin
	 * 
	 * @param name
	 *           DOCUMENT ME!
	 */
	public void setSkin(String skinName)
	{
		try
		{
			// Try setting it as a skinlf skin
			String fullSkinName = SKINDIR + File.separator + skinName + ".zip";
			logger.info("Setting skin: " + fullSkinName);
			SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(fullSkinName));
			//This method is deprecated, but it doesnt seem to work properly without it.
			SkinLookAndFeel.enable();

			// Update the components
			updateComponents();
		}
		catch (Exception ex)
		{
			logger.severe("Could not set skin: " + ex.toString());
		}
	}

	/**
	 * Add top level component to update when skin changes
	 * 
	 * @param component
	 *           DOCUMENT ME!
	 */
	public void addComponent(Component component)
	{
		components.add(component);
	}

	/**
	 * Update components
	 */
	private void updateComponents()
	{
		Iterator iter = components.iterator();

		while (iter.hasNext())
		{
			Component c = (Component) iter.next();
			logger.info("Updating component: " + c.getName());
			SwingUtilities.updateComponentTreeUI(c);
		}
	}

	/**
	 * Filter for skin files
	 */
	private class SkinFilter implements FilenameFilter
	{
		/**
		 * DOCUMENT ME!
		 * 
		 * @param dir
		 *           DOCUMENT ME!
		 * @param name
		 *           DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 */
		public boolean accept(File dir, String name)
		{
			if (name.toLowerCase().endsWith(".zip"))
				return true;
			else
				return false;
		}
	}
}
