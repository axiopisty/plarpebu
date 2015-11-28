package com.plarpebu;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;

import fr.unice.plugin.Plugin;

/**
 * MenuItem qui garde une r�f�rence � une instance de plugin. Doit-elle �tre public ??
 *
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
