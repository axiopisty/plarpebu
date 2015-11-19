package fr.unice.plugin;

/**
 * Minimal implementation of Plugin.
 * canProcess returns always true.
 * @author Richard Grin
 */
public abstract class AbstractPlugin implements Plugin {

  /**
   * Default implementation of matches.
   * @todo ?? use a regular expression for name?
   * @param type
   * @param name
   * @param object
   * @return true if types are identical, names are equal,
   * and if the plugin can process the object.
   */
  public boolean matches(Class type, String name, Object object) {
    if (type != null && type != this.getPluginType()) {
      return false;
    }
    if (name != null && ! name.equals(this.getName())) {
      return false;
    }
    if (object != null && ! this.canProcess(object)) {
      return false;
    }
    return true;
  }

  /**
   * Return always true: know how to process all objects.
   * @param object
   * @return true
   */
  public boolean canProcess(Object object) {
    return true;
  }

  public String getDescription() {
    return "Plugin; type: " + getPluginType() + "; name: " + getName()
        + "; can process all objects.";
  }

  public String getVersion() {
    return "";
  }


}