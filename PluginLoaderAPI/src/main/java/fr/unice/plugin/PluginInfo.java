package fr.unice.plugin;

import java.net.URL;

/**
 * Information about a plugin instance:
 * the instance and the URL from which it was loaded.
 * @todo Add the modification time (to allow reloading only if the version is
 * not the same).
 */
class PluginInfo {
  private Plugin pluginInstance;
  private URL loadingUrl;

  PluginInfo(Plugin pluginInstance, URL url) {
    this.pluginInstance = pluginInstance;
    this.loadingUrl = url;
  }

  Plugin getPluginInstance() {
    return pluginInstance;
  }

  URL getLoadingUrl() {
    return loadingUrl;
  }

  public boolean equals(Object object) {
    if (object.getClass() != this.getClass()) {
      return false;
    }
    PluginInfo anotherPluginInfo = (PluginInfo) object;
    if (this.loadingUrl.equals(anotherPluginInfo.loadingUrl)
        && this.pluginInstance.equals(anotherPluginInfo.pluginInstance)) {
      return true;
    }
    return false;
  }
}
