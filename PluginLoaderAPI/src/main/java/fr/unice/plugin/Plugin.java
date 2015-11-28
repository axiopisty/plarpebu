package fr.unice.plugin;

/**
 * Generic interface implemented by all the plugins.
 * Classes which implement this interface must provide also a constructor
 * without parameters (implicitly or not).
 *
 * @author Richard Grin (from the version of Michel Buffa)
 * @version 2.0 7/12/02
 */
public interface Plugin {

  /**
   * @return plugin name.
   */
  public String getName();

  /**
   * The type of the plugin must be a subtype of return type.
   *
   * @return plugin type
   */
  public Class getPluginType();

  /**
   * Returns a description of the plugin.
   *
   * @return the description
   */
  public String getDescription();

  /**
   * Returns the version of the plugin.
   *
   * @return the version.
   */
  public String getVersion();

  /*
   * Returns properties of the plugin. For example, a choice of a menu,
   * or the width of a drawing; you can imagine any things.
   * Each property is identified by a key in the map and its value is the value
   * of the key.
   * @return properties in the Map.
   */
  //  public Map getProperties();

  /**
   * Indicate if the plugin can process an object.
   * It can be used in the case where several plugins could implement a
   * functionality but with different conditions.
   * According to the model of the JDBC drivers.
   * The PluginManager can ask to several plugins of the same type
   * whether they can process an object.
   *
   * @return true if the plugin can process the object.
   */
  public boolean canProcess(Object object);

  /**
   * Returns true if the plugin matches the type, name and can process the
   * object passed in the parameters.
   * Method used by the plugin loaders to know if a plugin must be loaded or not.
   * Basic implementation (of AbstractPlugin) :
   * <pre>
   * public boolean matches(Class type, String name, Object object) {
   *   if (type != null && type != this.getPluginType()) {
   *     return false;
   *   }
   *   if (name != null && ! name.equals(this.getName())) {
   *     return false;
   *   }
   *   if (object != null && ! this.canProcess(object)) {
   *     return false;
   *   }
   *   return true;
   * }
   * </pre>
   *
   * @param type   type type of the plugin. No constraint if null.
   * @param name   name of the plugin. No constraint if null.
   * @param object object the plugin can process. No constraint if null.
   * @return true if the plugin matches the constraints.
   */
  public boolean matches(Class type, String name, Object object);

  /*
   * Returns true if the plugin matches the type, name, properties
   * and can process the object passed in the parameters.
   * Method used by the plugin loaders to know if a plugin must be loaded or not.
   * For the properties, it means that the plugin has all the properties
   * in the Map, with the same values. The instance can have other properties.
   * null value for a property of the map means "the plugin must have this
   * property, with any value for it" ???
   * @param type
   * @param name
   * @param object
   * @param properties
   * @return
   */
  // For another iteration if it's necessary.
  //  public boolean matches(Class type, String name,
  //                         Object object, Map properties);
}
