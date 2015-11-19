package fr.unice.plugin;

import java.util.EventObject;

public class PluginManagerEvent extends EventObject {
  Plugin plugin;

  public PluginManagerEvent(Plugin plugin) {
    super(plugin);
    this.plugin = plugin;
  }

  public Plugin getPlugin() {
    return this.plugin;
  }

  public void setPlugin(Plugin plugin) {
    this.plugin = plugin;
  }
}
