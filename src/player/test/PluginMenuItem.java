package player.test;

import javax.swing.*;

import pluginsSDK.*;
import fr.unice.plugin.Plugin;


/**
 * MenuItem qui garde une référence à une instance de plugin.
 * Doit-elle être public ??
 * @author Richard Grin
 * @version 1.0
 */
public class PluginMenuItem extends JCheckBoxMenuItem {
  private Plugin plugin;

  public PluginMenuItem() {
    super();
  }

  public PluginMenuItem(String text) {
    super(text);
  }

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
