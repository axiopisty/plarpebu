package com.plarpebu.plugins.sdk;

import javax.swing.JPanel;

/**
 * La classe PanelPlugin est la classe mere de tous les plugins qui se rajoutent dans l'interface
 * principale de notre lecteur mp3 sans pour autant creer une nouvelle fenetre. Elle etend Jpanel et
 * doit implementer bien evidemment playerPlugin. Cette classe est logiquement abstraite et nous
 * servira pour le polymorphisme.
 * 
 * @author ARNAUD & PEPINO
 */
public abstract class PanelPlugin extends JPanel implements PlayerPlugin
{

	/**
	 * Cette fonction retourne le type du plugin.
	 * 
	 * @return la classe du plugin.
	 */
	public Class getType()
	{
		return PanelPlugin.class;
	}

	/**
	 * Cette fonction permet d'obtenir une description du plugin
	 * 
	 * @return la description du plugin
	 */
	public String getDescription()
	{
		return "no Description";
	}

	/**
	 * Cette fonction permet d'obtenir une description du plugin
	 * 
	 * @return la description du plugin
	 */
	public boolean canProcess(Object o)
	{
		return true;
	}

	public boolean matches(Class type, String name, Object object)
	{
		return true;
	}

	public String getVersion()
	{
		return "1.0";
	}
}
