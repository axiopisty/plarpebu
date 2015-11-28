package fr.unice.plugin;

import java.util.EventListener;

public interface PluginManagerListener extends EventListener {

  void pluginLoaded(PluginManagerEvent var1);
}
