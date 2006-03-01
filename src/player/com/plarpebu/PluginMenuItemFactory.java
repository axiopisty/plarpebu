package player.test;

import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JMenu;

import fr.unice.plugin.Plugin;
import fr.unice.plugin.PluginManager;

/**
 * Classe qui met dans un menu des items liées aux plugins déjà chargés. Une
 * instance de cette classe s'occupe toujours du même menu qui peut être
 * reconstruit plusieurs fois durant une session de travail. Pour être clair, le
 * JMenu est toujours le même mais ses entrées sont reconstruites à chaque appel
 * de la méthode buildMenu. Les entrées tiennent compte des plugins déjà
 * chargées au moment de l'appel de builMenu. S'il y a des nouveaux plugins, de
 * nouvelles entrées sont ajoutées au menu. Si des plugins ont disparu, les
 * entrées correspondantes ne seront plus dans le menu. Mais de nouvelle Si les
 * plugins ont changé, un client peut reconstruire le menu. En ce cas, les
 * anciennes entrées liées aux plugins sont enlevées et les nouvelles sont
 * ajoutées au même endroit dans le menu.
 * <P>
 * Exemple d'utilisation :
 * 
 * <pre>
 *  pluginManager = PluginManager.getPluginManager(
 *      &quot;file:repplugin/plugins.jar&quot;);
 *  pluginManager.loadPlugins();
 *  JMenuBar mb = new JMenuBar();
 *  menuPlugins = new JMenu(&quot;Plugins&quot;);
 *  menuPlugins.add(...);
 *  menuPlugins.addSeparator();
 *  PluginMenuFactory pluginMenuFactory = new PluginMenuFactory(menuPlugins);
 *  // Construit un menu avec tous les plugins de type Dessinateur.
 *  menuPlugins = pluginMenuFactory.buildMenu(Dessinateur.class);
 *  menuPlugins.add(...);
 *  mb.add(menuPlugins);
 *  // Met un dessinateur par défaut (le 1er dans la liste des entrées du menu)
 *  dessinateur = (Dessinateur)pluginMenuFactory.getPlugin(0);
 *  pluginMenuFactory.addObserver(this);
 *  . . .
 *  public void update(Observable o, Object arg) {
 *    fenetreDessin.setDessinateur((Dessinateur)arg);
 *  }
 * </pre>
 * 
 * @author Richard Grin
 * @version 1.0
 */
public class PluginMenuItemFactory {
    /**
     * Le menu géré par cette instance.
     */
    private JMenu menu;

    /**
     * Le chargeur de classes charge les plugins.
     */
    private PluginManager loader;

    /**
     * L'actionListener qui va écouter les entrées du menu des plugins.
     */
    private ActionListener listener;

    private static Logger logger = Logger.getLogger("player.test.PluginMenu");

    /**
     * Construit une instance qui concerne un certain menu. Ce menu aura des
     * choix qui permettront de sélectionner un plugin ou un autre.
     * 
     * @param menu
     *        le menu géré par cette instance.
     * @param loader
     *        le chargeur de classes des plugins.
     * @param listener
     *        l'actionDeListener qui va écouter les entrées du menu.
     */
    public PluginMenuItemFactory(JMenu menu, PluginManager loader, ActionListener listener) {
        this.menu = menu;
        this.loader = loader;
        this.listener = listener;
    }

    /**
     * Construit le menu des plugins.
     * 
     * @param type
     *        type des plugins utilisés pour construire le menu. Si null, tous
     *        les types de plugin seront utilisés pour construire le menu.
     * @return le menu construit avec les plugins.
     */
    public void buildMenu(Class type) {
        if (loader == null) {
            return;
        }
        logger.info("Construction du menu des PLUGINS");

        // Enlève les entrées précédentes s'il y en avait
        menu.removeAll();

        // Récupère les instances déjà chargées

        Plugin[] instancesPlugins = loader.getPluginInstances(type);
        logger.info("Nombre de plugins trouvés :" + instancesPlugins.length);
        PluginMenuItem item;
        // On ajoute une entrée par instance de plugin
        for (int i = 0; i < instancesPlugins.length; i++) {
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
    public JMenu getMenu() {
        return menu;
    }
}
