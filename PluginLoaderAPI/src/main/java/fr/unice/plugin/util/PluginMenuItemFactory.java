package fr.unice.plugin.util;

import fr.unice.plugin.Plugin;
import fr.unice.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

/**
 * This class facilitate the creation and usage
 * of menus with choices associated with plugins.
 * Clients must register as an observer of this class to be warned
 * if the current plugin is changed.
 * The menu can contain other entries not associated with plugins.
 * When buildMenu is called, the plugin entries are added
 * at a location given by the int parameter n.
 * If plugin have changed, the menu can be rebuilt.
 * <p>
 * Example:
 * <pre>
 * pluginManager = PluginManager.getPluginManager(
 *     "file:repplugin/plugins.jar");
 * pluginManager.loadPlugins();
 * JMenuBar mb = new JMenuBar();
 * menuPlugins = new JMenu("Plugins");
 * menuPlugins.add(...);
 * menuPlugins.addSeparator();
 * PluginMenuFactory pluginMenuFactory = new PluginMenuFactory(menuPlugins);
 * // Build a menu with entries for the plugins of type Drawing.
 * menuPlugins = pluginMenuFactory.buildMenu(Drawing.class);
 * menuPlugins.add(...);
 * mb.add(menuPlugins);
 * // The first entry is the default plugin
 * drawing = (Drawing)pluginMenuFactory.getPlugin(0);
 * pluginMenuFactory.addObserver(this);
 * . . .
 * public void update(Observable o, Object arg) {
 *   fenetreDessin.setDrawing((Drawing)arg);
 * }
 * </pre>
 *
 * @author Richard Grin
 * @version 1.0
 */
public class PluginMenuItemFactory extends Observable implements ActionListener {

  /**
   * Menu managed by this instance.
   */
  private JMenu menu;

  private static Logger logger = LoggerFactory.getLogger(PluginMenuItemFactory.class);

  /**
   * menu will have entries to select one plugin or another.
   *
   * @param menu menu managed by this instance.
   */
  public PluginMenuItemFactory(JMenu menu) {
    this.menu = menu;
  }

  /**
   * Builds the menu.
   *
   * @param type type of the plugins used by the menu.
   * @param n    index where plugin entries are inserted.
   * @return built menu.
   */
  public void buildMenu(Class type, int n) {
    PluginManager pluginManager = PluginManager.getPluginManager();
    if(pluginManager == null) {
      return;
    }
    logger.trace("Build the plugin menu");

    Plugin[] instancesPlugins = pluginManager.getPluginInstances(type);
    //    Plugin[] instancesPlugins = pluginManager.getPluginInstances();
    logger.debug("Number of plugins found:" + instancesPlugins.length);

    // Add one entry per plugin
    for(int i = 0; i < instancesPlugins.length; i++) {
      Plugin plugin = instancesPlugins[i];
      String nomPlugin = plugin.getName();
      PluginMenuItem mi = new PluginMenuItem(nomPlugin, plugin);
      mi.addActionListener(this);
      menu.insert(mi, n);
    }
  }

  public void buildMenu(Class type) {
    buildMenu(type, 0);
  }

  public void buildMenu(int n) {
    buildMenu(null, n);
  }

  public void buildMenu() {
    buildMenu(null, 0);
  }

  /**
   * Rebuild the menu.
   * First remove plugin entries, and then add entries for each of the plugins
   * currently registered with the type.
   * If there is no plugin entry, the new entries are added at the end of
   * the menu.
   *
   * @param type type of the plugins
   */
  public void rebuildMenu(Class type) {
    // 1. Remove entries
    int n = menu.getMenuComponentCount();
    // Keep the index of the last plugin entry.
    int firstPluginMenuItemNumber = n;
    // entries are removed beginning with the last plugin entry
    for(int i = n - 1; i >= 0; i--) {
      Component menuItem = menu.getMenuComponent(i);
      if(!(menuItem instanceof PluginMenuItem)) {
        // not a plugin entry
        continue;
      }
      Plugin plugin = ((PluginMenuItem) menuItem).getPlugin();
      if(type != null) {
        if(plugin.getPluginType() == type) {
          menu.remove(i);
          firstPluginMenuItemNumber = i;
        }
      } else {
        menu.remove(i);
        firstPluginMenuItemNumber = i;
      }
    }
    // add plugin entries from index firstPluginMenuItemNumber.
    buildMenu(type, firstPluginMenuItemNumber);
  }

  /**
   * Process actions.
   */
  public void actionPerformed(ActionEvent e) {
    Plugin chosenPlugin;
    chosenPlugin = ((PluginMenuItem) e.getSource()).getPlugin();
    // observers are notified
    setChanged();
    notifyObservers(chosenPlugin);
  }
}