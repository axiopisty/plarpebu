package com.plarpebu;

import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JMenu;

import fr.unice.plugin.Plugin;
import fr.unice.plugin.PluginManager;

import static com.plarpebu.common.PlarpebuUtil.configureLogToFile;

/**
 * Classe qui met dans un menu des items li�es aux plugins d�j� charg�s. Une instance de cette
 * classe s'occupe toujours du m�me menu qui peut �tre reconstruit plusieurs fois durant une session
 * de travail. Pour �tre clair, le JMenu est toujours le m�me mais ses entr�es sont reconstruites �
 * chaque appel de la m�thode buildMenu. Les entr�es tiennent compte des plugins d�j� charg�es au
 * moment de l'appel de builMenu. S'il y a des nouveaux plugins, de nouvelles entr�es sont ajout�es
 * au menu. Si des plugins ont disparu, les entr�es correspondantes ne seront plus dans le menu.
 * Mais de nouvelle Si les plugins ont chang�, un client peut reconstruire le menu. En ce cas, les
 * anciennes entr�es li�es aux plugins sont enlev�es et les nouvelles sont ajout�es au m�me endroit
 * dans le menu.
 * <P>
 * Exemple d'utilisation :
 * 
 * <pre>
 *   pluginManager = PluginManager.getPluginManager(
 *       &quot;file:repplugin/plugins.jar&quot;);
 *   pluginManager.loadPlugins();
 *   JMenuBar mb = new JMenuBar();
 *   menuPlugins = new JMenu(&quot;Plugins&quot;);
 *   menuPlugins.add(...);
 *   menuPlugins.addSeparator();
 *   PluginMenuFactory pluginMenuFactory = new PluginMenuFactory(menuPlugins);
 *   // Construit un menu avec tous les plugins de type Dessinateur.
 *   menuPlugins = pluginMenuFactory.buildMenu(Dessinateur.class);
 *   menuPlugins.add(...);
 *   mb.add(menuPlugins);
 *   // Met un dessinateur par d�faut (le 1er dans la liste des entr�es du menu)
 *   dessinateur = (Dessinateur)pluginMenuFactory.getPlugin(0);
 *   pluginMenuFactory.addObserver(this);
 *   . . .
 *   public void update(Observable o, Object arg) {
 *     fenetreDessin.setDessinateur((Dessinateur)arg);
 *   }
 * </pre>
 * 
 * @author Richard Grin
 * @version 1.0
 */
public class PluginMenuItemFactory
{
	/**
	 * Le menu g�r� par cette instance.
	 */
	private JMenu menu;

	/**
	 * Le chargeur de classes charge les plugins.
	 */
	private PluginManager loader;

	/**
	 * L'actionListener qui va �couter les entr�es du menu des plugins.
	 */
	private ActionListener listener;

	private static Logger logger = configureLogToFile(Logger.getLogger("player.test.PluginMenu"));

	/**
	 * Construit une instance qui concerne un certain menu. Ce menu aura des choix qui permettront de
	 * s�lectionner un plugin ou un autre.
	 * 
	 * @param menu
	 *           le menu g�r� par cette instance.
	 * @param loader
	 *           le chargeur de classes des plugins.
	 * @param listener
	 *           l'actionDeListener qui va �couter les entr�es du menu.
	 */
	public PluginMenuItemFactory(JMenu menu, PluginManager loader, ActionListener listener)
	{
		this.menu = menu;
		this.loader = loader;
		this.listener = listener;
	}

	/**
	 * Construit le menu des plugins.
	 * 
	 * @param type
	 *           type des plugins utilis�s pour construire le menu. Si null, tous les types de plugin
	 *           seront utilis�s pour construire le menu.
	 * @return le menu construit avec les plugins.
	 */
	public void buildMenu(Class type)
	{
		if (loader == null)
		{
			return;
		}
		logger.info("Construction du menu des PLUGINS");

		// Enl�ve les entr�es pr�c�dentes s'il y en avait
		menu.removeAll();

		// R�cup�re les instances d�j� charg�es

		Plugin[] instancesPlugins = loader.getPluginInstances(type);
		logger.info("Nombre de plugins trouv�s :" + instancesPlugins.length);
		PluginMenuItem item;
		// On ajoute une entr�e par instance de plugin
		for (int i = 0; i < instancesPlugins.length; i++)
		{
			Plugin plugin = instancesPlugins[i];

			String mi = plugin.getName();
			item = new PluginMenuItem(mi);
			item.setPlugin(instancesPlugins[i]);
			menu.add(item);

			// menu.add(mi);
			item.addActionListener(listener);

		}
	}

	/**
	 * l'accesseur de la donnee JMenu
	 * 
	 * @return le JMenu
	 */
	public JMenu getMenu()
	{
		return menu;
	}
}
