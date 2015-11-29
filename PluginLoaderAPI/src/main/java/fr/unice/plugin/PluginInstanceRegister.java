package fr.unice.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Registry for the plugin instances already loaded.
 * Contains only one instance of each class.
 * Hide the structure used to store the plugin instances
 * (choice is not clear; a Map for the moment).
 *
 * @author Richard Grin
 * @version 1.0
 */

class PluginInstanceRegister {

  /**
   * The key of the Map is the qualified name of the class of the plugin,
   * the value is the PluginInfo which contains the plugin and the URL from
   * which the plugin was loaded.
   */
  // Why a map and not a list? To verify more quickly if a plugin with this
  // this class has already been registered (used in register).
  // If another structure is used, 3 m�thods must be modified:
  //  - unregisterPluginInstancesFrom
  //  - register
  //  - getPluginInfoIterator
  private Map pluginInstances = new HashMap();

  private static final Logger logger = LoggerFactory.getLogger(PluginInstanceRegister.class);

  /**
   * Register PluginInfo instances.
   * Don't register an instance if a plugin of the same class is already
   * registered, even if the plugin has been loaded from another URL.
   * ** Is this rule good??? **
   *
   * @param instances plugin instances to register.
   */
  void register(List instances) {
    for(int i = 0; i < instances.size(); i++) {
      Object instance = instances.get(i);
      if(instance instanceof PluginInfo) {
        /* Don't register a pluginInfo if a plugin of the same class is already
           registered. */
        register((PluginInfo) instance);
      }
    }
  }

  void register(PluginInfo[] instances) {
    for(int i = 0; i < instances.length; i++) {
      register(instances[i]);
    }
  }

  /**
   * Register a PluginInfo instance.
   * Don't register an instance if a plugin of the same class is already
   * registered, even if the plugin has been loaded from another URL.
   *
   * @param instance instance to register.
   * @return true if the instance has been registered
   */
  boolean register(PluginInfo instance) {
    /*
     Only one instance for one plugin class qualified name. Good choice??
    */
    String className = ((PluginInfo) instance).getPluginInstance().getClass().getName();
    if(!pluginInstances.containsKey(className)) {
      pluginInstances.put(className, instance);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns all the plugin instances registered, with a type, name and
   * which can process an object.
   *
   * @param type   type of the searched plugin. Any type if null.
   * @param name   name of the searched plugin. Any name if null.
   * @param object the instance must be able to process this objet. Don't
   *               do any selection if null.
   * @return a fully filled array (no null elements) of searched plugin instances.
   * The type of the array is "type"[] where "type" is the first parameter
   * of the method (if not null).
   */
  Plugin[] getPluginInstances(Class type, String name, Object object) {
    logger.debug("type=" + type + "; name=" + name + "; object=" + object);
    ArrayList instances = new ArrayList();
    //    Iterator it = pluginInstances.values().pluginInfos.iterator();
    Iterator it = getPluginInfoIterator();
    while(it.hasNext()) {
      Plugin instance = ((PluginInfo) it.next()).getPluginInstance();
      logger.trace("instance examin�e=" + instance);
      if(instance.matches(type, name, object)) {
        logger.trace("instance qui convient=" + instance);
        instances.add(instance);
      }
    }
    // Le tableau renvoy� a le type pass� en param�tre
    if(type == null) {
      return (Plugin[]) (instances.toArray(new Plugin[0]));
    } else {
      // Create an array whose type is parameter type
      Plugin[] array = (Plugin[]) Array.newInstance(type, instances.size());
      return (Plugin[]) instances.toArray(array);
    }
    // If one doesn't want to use reflexivity, replace the previous if by :
    //    return (Plugin[]) (instances.toArray(new Plugin[0]));
  }

  /**
   * Return all the plugin instances registered.
   *
   * @return a full array (no null elements) of the plugin instances.
   */
  Plugin[] getPluginInstances() {
    return getPluginInstances(null, null, null);
  }

  Plugin[] getPluginInstances(Class type) {
    return getPluginInstances(type, null, null);
  }

  Plugin[] getPluginInstances(Class type, String name) {
    return getPluginInstances(type, name, null);
  }

  Plugin[] getPluginInstances(Class type, Object object) {
    return getPluginInstances(type, null, object);
  }

  void clear() {
    pluginInstances.clear();
  }

  /**
   * Unregister plugin instances loaded from some URLs.
   * These instances will be still used by those which have got
   * a reference of them but they will not be available for others.
   *
   * @param urls array filled with URLs.
   * @return plugin instances removed.
   */
  // A MODIFIER si je change de structure de donn�es pour enregistrer
  // les plugins.
  List unregisterPluginInstancesFrom(URL[] urls) {
    List removedPlugins = new ArrayList();
    Iterator it = pluginInstances.keySet().iterator();
    while(it.hasNext()) {
      PluginInfo pluginInfo = (PluginInfo) it.next();
      for(int i = 0; i < urls.length; i++) {
        if(pluginInfo.getLoadingUrl().equals(urls[i])) {
          pluginInstances.remove(pluginInfo);
          removedPlugins.add(pluginInfo.getPluginInstance());
        } // if
      } // for
    } // while
    return removedPlugins;
  }

  public String toString() {
    return "Number of registered plugins = " + pluginInstances.size();
  }

  /**
   * This method isolates the choice of a data structure for registering
   * plugin information.
   *
   * @return an iterator for iterating all the pluginInfo instances.
   */
  private Iterator getPluginInfoIterator() {
    return pluginInstances.values().iterator();
  }
}