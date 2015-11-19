package fr.unice.plugin.util;

import javax.swing.JMenuItem;
import javax.swing.Icon;
import fr.unice.plugin.*;

/**
 * MenuItem which keep a reference to a plugin instance.
 * Has it to be public or not?
 * @author Richard Grin
 * @version 1.0
 */
public class PluginMenuItem extends JMenuItem {
  private Plugin plugin;

  public PluginMenuItem() {
    super();
  }

  public PluginMenuItem(String text) {
    super(text);
  }

  /**
   *
   * @param text text of the item
   * @param plugin plugin instance
   */
  public PluginMenuItem(String text, Plugin plugin) {
    super(text);
    this.plugin = plugin;
  }

  public PluginMenuItem(String text, Icon icon) {
    super(text, icon);
  }

  public void setPlugin(Plugin plugin) {
    this.plugin = plugin;
  }

  public Plugin getPlugin() {
    return plugin;
  }
}