package fr.unice.plugin;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;

import fr.unice.grin.detectjar.DetectJar;
import java.io.FilenameFilter;
import java.util.*;

import static com.plarpebu.common.PlarpebuUtil.configureLogToFile;

/**
 * Manage plugins:
 * <ul>
 *   <li> load and register plugins located at some URLs;
 *   <li> return all the plugins or only those which satisfy a criteria
 *        (name, type of the plugins or objects the plugins can process).
 * </ul>
 * It's a singleton: only one PluginManager per application.
 * <P>
 * Only this class is known by the clients which use the plugins.
 * It manages the URLs where the plugins are located (add or remove URLs).
 * <P>
 * A plugin class must be stored under one of these URLs, in a location
 * compatible with the package name of the class.
 * For example, if the URL is <code>file:rep1/rep2/</code>, the plugin class
 * <code>foo.thing.P1</code> must be stored in
 * <code>rep1/rep2/foo/thing/P1.class</code>
 * <P>
 * Plugins already loaded are not reloaded, even if a new version has been put
 * in one of the URLs, except if the refresh method is called.
 * <P>
 * After the refresh method is called, new versions of the plugins are loaded
 * (except if an old version is found before in a URL).
 * <P>
 * If 2 URLs contain 2 plugins with the same classname, only one class will be
 * loaded (the first found).
 * <P>
 * How to use this class:
 * <ol>
 * <li> Create a PluginManager with the static method getPluginManager.
 *    You pass the URLs where the plugin classes will be searched.
 * <li> Request to the pluginManager to load the plugins. The new plugins (not
 *    already loaded will be loaded.
 *    The new versions of the plugins will not be loaded.
 * <li> To get some of the loaded plugins, request the pluginManager.
 * </ol>
 *
 * 3 types of URLs:
 * <ul>
 * <li> jar files (local ou remote)
 * <li> local directories (URL <b>MUST</b> end with "/")
 * <li> .class files (in the current version, the class must not be in a package).
 * </ul>
 * <P>
 *
 * Format of the URLs:
 * <ul>
 * <li> for a local jar: <code>jar:file:plugins.jar!/</code>
 * for a remote jar : <code>jar:http://deptinfo.unice.fr/~grin/plugins/truc.jar!/</code>
 * <li> for a local directory: <code>file:rep/plugins/</code> (don't forget the
 * final "/") ;
 * <li> for a class : <code>file:/rep/plugins/Plugin2.class</code>.
 * </ul>
 * If the path is relative, it's relative to the current directory,
 * ie the directory from which the application has been launched.
 * <P>
 * Code example:
 * <pre>
 * // Get the plugin manager
 * pluginManager = PluginManager.getPluginManager(
 *      new URL[] { new URL("file:repplugin/rep/"),
 *                  new URL("jar:file:repplugin/plugins.jar!/") };
 * // Ask the plugin manager to load the plugins
 * pluginManager.loadPlugins();
 * // Get some of the loaded plugins (those with a type)
 * Plugin[] instancesPlugins = pluginManager.getPluginInstances(type);
 * </pre>
 *
 * To facilitate usage of the API, it is possible to use String instead of URLs.
 * In this case, it's allowed to make some mistakes in the format of the URLs.
 * For example, you can forget the final
 * "/" or "!/" as they are automaticaly added:
 * <pre>
 *  pluginManager = PluginManager.getPluginManager(
 *      new String[] { "file:repplugin/plugins.jar", ... });
 * </pre>
 * or
 * <pre>
 * pluginManager = PluginManager.getPluginManager(
 *     "file:repplugin/plugins.jar");
 * </pre>
 * <P>
 * It is also possible to add all the jars placed in some directories in one
 * instruction:
 * <pre>
 * pluginManager = PluginManager.getPluginManager();
 * pluginManager.addJarURLsInDirectories(new URL[] { new URL("file:rep1") });
 * </pre>
 * <b>Take care</b>, the directories that contain plugins must be entries in the jar.
 * With the example below, the jar will have to contain a rep/plugins entry.
 * else the resource /dir/plugins will not be found.
 * Example:
 * <pre>
 * String  directoryName = "/dir/plugins/";
 * pluginManager = PluginManager.getPluginManager(directoryName);
 * </pre>
 * The execution is logged to help debugging.
 * To cut all the messages, put in the main class
 * <pre>
 * Logger logger = Logger.getLogger("fr.unice.plugin.PluginManager");
 * Logger.getLogger("fr.unice.plugin").setLevel(Level.SEVERE);
 * </pre>
 * To get all the messages, put in the main class
 * <pre>
 * Logger.getLogger("fr.unice.plugin").setLevel(Level.WARNING);
 * </pre>
 * @author Richard Grin
 * @version 1.0
 */
public class PluginManager {
  /* Delegate the loading of the plugins classes to a PluginLoader which create
   * one instance of each plugin class.
   * A registry register the loaded plugin instances, to provide them, and
   * without another loading to the clients they need them.
   */

  /**
   * Contains the information about the loaded plugins.
   */
  private PluginInstanceRegister pluginInstanceRegister =
      new PluginInstanceRegister();
  /**
   * Plugin loader to which the loading is delegated.
   */
  private PluginLoader pluginLoader;
  /**
   * Contains URL where to search plugins.
   * No doubles.
   * Currently the list contain only URLs. To add flexibility
   * it is planned to store here more complex information in a next version.
   * For example, to indicate that URLs must be recursively searched,
   * or that the loader must load all the plugins contained in all the jars
   * within the URL. The syntax for that to indicate the URLs could be like
   * the syntax for the security domains (/, /* or /-).
   */
  private List urls = new ArrayList();

  private static PluginManager pluginManager;

  private static Logger logger = configureLogToFile(Logger.getLogger("fr.unice.plugin.PluginManager"));

  /**
   * Private constructor for the singleton pattern.
   * @param urls URLs where the plugins will be searched.
   */
  private PluginManager(URL[] urls) {
    logger.info("Length of the array of the URLs: " + urls.length);
    for (int i = 0; i < urls.length; i++) {
      logger.info("Add one URL " + urls[i]);
      this.urls.add(urls[i]);
    }
    pluginLoader = new PluginLoader(urls);
  }

  private PluginManager() {
    pluginLoader = new PluginLoader();
  }

  /**
   * Returns the current PluginManager.
   * @return the current PluginManager. Creates it if it does not exist.
   */
  public static PluginManager getPluginManager() {
    if (pluginManager == null) {
      pluginManager = new PluginManager();
    }
    return pluginManager;
  }

  /**
   * Returns the plugin manager. It exists only one plugin manager.
   * <P>
   * If it does not exist, it is created with the paths newUrls to search
   * the plugins.
   * <P>
   * If it already exists, the URLs are added to the search path.
   * @param newUrls URLs where the plugins will be searched. The array is
   * full (no null URL).
   * @return the pluginManager.
   */
  public static PluginManager getPluginManager(URL[] newUrls) {
    /* Perhaps it would be better to remove the parameter because the client
     * can use addURL to add URLs?). ** TO SEE **
     */

    if (pluginManager == null) {
      pluginManager = new PluginManager(newUrls);
    }
    else {
      pluginManager.addURL(newUrls);
    }
    return pluginManager;
  }

  /**
   * URLs are in a single String with the delimiter ";".
   * @param urlsString contain the URLs
   * @return le pluginManager qui cherchera dans ces URLs
   */
  public static PluginManager getPluginManager(String urlsString) {
    /* ":" can not be the delimiter because URLs contain it. */
    String[] urls = urlsString.split(";");
    return getPluginManager(urls);
  }

  /**
   * URLs are in a collection.
   * @see #getPluginManager(URL[])
   * @param newUrls collection that contains the URLs where the plugins will be
   * searched.
   * @return the pluginManager
   */
  public static PluginManager getPluginManager(Collection newUrls) {
    return getPluginManager((URL[])newUrls.toArray(new URL[0]));

  }

  /**
   * Convenient method in case of the URLs are in strings.
   * Some syntax errors are corrected:
   * <ul>
   *   <li> if the URL contains some \, they are replaces by / (to help Windows
   *     users);
   *   <li> if the URL contains some %20, it is replaced by a space;
   *   <li> if a URL has no protocol at the beginning,
   *     <ul>
   *     <li> if it ends by ".jar", "jar:" is added at the beginning if it is
   *          missing;
   *     <li> if it is a jar, but it does not contain !/, it is added at the
   *          good place (after the name of the jar file and before the name
   *          of the jar entry);
   *     <li> if it ends by .class, but does not begin by file:, it is added;
   *     <li> if it not a jar neither a .class, it is considered as a directory.
   *       <ul>
   *       <li> if the URL begins by file:, it is not modified
   *       <li> else, the URL is modified. If the name is absolute, the URL is
   *         considered as a resource (searches are made relatively to the
   *         classpath).
   *         If the name is relative, and the application is in a jar (detected
   *         with DetectJar), it is guessed that the user wants to point to
   *         the a path relatively the root of the jar.
   *         If the name is relative, and the application is not in a jar, file:
   *         is added at the beginning.
   *       </ul>
   *    </ul>
   * </ul>
   * @see #getPluginManager(URL[])
   * @param newUrls contains the URL where the plugins will be searched.
   * @return the pluginManager
   */
  public static PluginManager getPluginManager(String[] newUrls) {
    URL[] urlt = new URL[newUrls.length];
    int nbUrls = 0;
    for (int i = 0; i < newUrls.length; i++) {
      // Correction are made if possible
      // If a URL is malformed, URL is not added; client will receive an error
      // message by getURL
      try {
        logger.info("Add URL *" + newUrls[i] + "* for the search of the plugins");
        urlt[i] = getURL(newUrls[i]);
        nbUrls++;
      }
      catch(MalformedURLException ex) {
        ex.printStackTrace();
      }
    }
    // array must be full (without any null)
    if (nbUrls != newUrls.length) {
      // URLs was malformed; compact the array
      URL[] urlt2 = new URL[nbUrls];
      System.arraycopy(urlt, 0, urlt2, 0, nbUrls);
      urlt = urlt2;
    }
    return getPluginManager(urlt);
  }

  /**
   * Convenient method to correct syntax errors in a URL.
   * Sometimes one must guess the good URL and sometimes it is necessary to
   * make a choice (perhaps a bad one...).
   * What it is done in this method:
   * <ul>
   *   <li> if the URL contains some \, they are replaces by / (to help Windows
   *     users);
   *   <li> if the URL contains some %20, it is replaced by a space;
   *   <li> if a URL has no protocol at the beginning,
   *     <ul>
   *     <li> if it ends by ".jar", "jar:" is added at the beginning;
   *     <li> if it is a jar, but it does not contain !/, it is added at the
   *          good place (after the name of the jar file and before the name
   *          of the jar entry);
   *     <li> if it ends by .class, but does not begin by file:, it is added;
   *     <li> if it not a jar neither a .class, it is considered as a directory.
   *       <ul>
   *       <li> if the URL begins by file:, it is not modified
   *       <li> else, the URL is modified. If the name is absolute, the URL is
   *         considered as a resource (searches are made relatively to the
   *         classpath)
   *         If the name is relative, and the application is in a jar (detected
   *         with DetectJar), it is guessed that the user wants to point to
   *         a path relatively to the root of the jar.
   *         If the name is relative, and the application is not in a jar, file:
   *         is added at the beginning.
   *        </ul>
   *      </ul>
   * </ul>
   * @param url a URL, perhaps with a syntax error.
   * @return the URL with a good syntax.
   * @throws MalformedURLException if it not possible to guess the good syntax.
   */
  private static URL getURL(String url) throws MalformedURLException {
    logger.info("URL non transform�e : " + url);

    /* Correct Windows name;
     * for example, C:\rep\machin becomes file:/C:/rep/machin
     */
    if (url.indexOf("\\") != -1) {
      // on peut soup�onner un nom Windows !
      // 4 \ pour obtenir \\ pour l'expression r�guli�re !
      url = url.replaceAll("\\\\", "/");
      // surtout s'il n'y a pas de protocole au d�but
//      if (! (url.startsWith("file:") || url.startsWith("jar:")
//             || url.startsWith("http:"))) {
//        url = url.replaceFirst("^([A-Za-z]):", "/$1/");
//      }
    } // Nom au format Windows

    /* Sometimes %20 can be at the place of space
     */
    url = url.replaceAll("%20", " ");

    int n;
    if ((n = url.indexOf(".jar")) != -1) { // C'est un jar
      /*
       * Format of a URL for a jar :
       * jar:file:repplugin/plugins.jar!/rep1/rep2
       * or (the root of the jar) :
       * jar:file:repplugin/plugins.jar!/
       */
      // if "jar:" is missing at the beginning, it is added
      if (! url.startsWith("jar:")) {
        // Case where "file:" is at the beginning
        if (! url.startsWith("file:")) {
            url = "file:" + url;
            n += 5;
        }
        url = "jar:" + url;
        n += 4; // for the next if
      }
      // if "!/" is missing just after .jar, it is added
      // If .jar is not at the end, there will be 2 "/" but it works nevertheless
      if (url.length() < n + 6 || url.substring(n + 4, n + 6) != "!/") {
        url = url.substring(0, n + 4) + "!/" + url.substring(n + 4);
      }
    }
    else { // it is not a jar
      if (url.endsWith(".class")) { // it is a class file
      // If there is no protocol, "file:" is added at the beginning
        if (! (url.startsWith("file:") || url.startsWith("jar:"))
               || url.startsWith("html://")) {
          url = "file:" + url;
        }
      } // a class file
      else { // it is a directory (no other possibility !)

        // Ne faudrait-il pas ajouter / � la fin s'il n'y est pas ????****


        /* C'est le cas o� il n'y a pas une solution �vidente
         * pour deviner ce que veut le client.
         * Voici ce qui a �t� choisi pour cette version dans les divers cas :
         *   - S'il y a un protocole, on ne fait rien ; par exemple,
         *     l'utilisateur indique que les plugins sont dans un r�pertoire
         *     avec un nom absolu et, dans ce cas, il doit mettre le protocole
         *     "file:" au d�but ;
         *   - S'il n'y a pas de protocole, plusieurs cas :
         *       . si le nom est absolu, on va consid�rer que l'utilisateur
         *         donne le nom d'un r�pertoire comme un nom de ressource. Ca
         *         permet ainsi de donner un nom de r�pertoire qui va marcher
         *         que l'application soit distribu�e dans un jar ou non.
         *         Autre stat�gie possible : on essaie de voir si un fichier
         *         de ce nom absolu existe dans le syst�me de fichier local
         *         avant de consid�rer que c'est un nom de ressource. Je ne
         *         l'ai pas fait. Le client devra mettre file: devant lui-m�me
         *         pour donner un nom de fichier absolu.
         *       . si le nom est relatif, le choix est difficile car mauvais
         *         de toute fa�on. J'ai choisi d'ajouter file: devant.
         *         L'explication suit :
         * Les raisons de ce choix : il n'est pas bon de donner un nom de
         * fichier relatif car au moment d'�crire le code
         * on ne sait pas quel sera le r�pertoire courant. De plus, �a ne
         * marche plus si l'application est dans un jar. On pourrait consid�rer
         * que, par un nom relatif, le client d�signe un fichier relativement
         * � la classe PluginManager. Mais �a n'est pas tr�s logique non plus !
         * surtout si PluginManager est distribu� dans un jar, � part de
         * l'application. A la rigueur, on pourrait penser que le client
         * donne un chemin relatif par rapport � une classe de l'application,
         * mais comment savoir laquelle ? Il faudrait qu'il la d�signe mais
         * �a serait trop lourd. D'o� le choix de mettre file: devant...
         * Pour un nom absolu, c'est plus facile : on consid�re que le client
         * donne un nom relatif au classpath (ce qui est fait pour getResource
         * de la classe Class), c'est-�-dire, par rapport � la racine du jar
         * si l'application est dans un jar et lanc�e par "java -jar ..."
         * Mais il y a eu du changement ; voir commentaires de la m�thode
         * (au-dessus de la m�thode).
         */
        if (! url.startsWith("file:")) {
          /* Absolute paths are considered as a resource name (a path relative
           * to the classpath).
           */
          if (url.startsWith("/")) { // Nom absolu
            logger.info("URL transform�e : ressource " + url);
            /* Next line is annoying as it constrains to have an entry for
             * the directory in the jar. But tools like JBuilder does not put
             * entries in the jar for directories that contains resources.
             * To do: if the application is in a jar, look into the jar
             * all the entries that begin with url.
             */
//            logger.info("Resource exists? " + new File(url2.getPath()).exists());
//            logger.info("equivalent ? " + new File(url2.getPath()).toURL().equals(url2));
            logger.info("Classpath = " + System.getProperty("java.class.path"));
            URL url2 = PluginManager.class.getResource(url);
            if (url2 != null) {
              logger.info("URL of the resource: " + url2);
              return url2;
            }
            else {
              // ressource does not exist
              throw new MalformedURLException("Resource " + url
                                              + " does not exist!");
            }
          }  // url begins with "/"
          else { // Relative name
           // Gets the URL of the main class of the application (the one that
           // contains the main method).
            URL mainClassUrl = DetectJar.mainClassURL();
            // if it has a jar protocol, the application is in a jar
            if (mainClassUrl.getProtocol().equals("jar")) {
              String urlName = mainClassUrl.getPath();
              // The URL parameter is condired as the path of a file inside
              // the jar, relative to the root of the jar.
              url = "jar:" + urlName.substring(0, urlName.indexOf('!') + 2)
                    + url;
              logger.info("Name of a directory relative to a jar" + url);
            }
            else {
              // If it is not in a jar, file: is added at the beginning
              url = "file:" + url;
            }
          }
        } // does not begin with file:
        if (! url.endsWith("/")) {
          // Faut-il le faire aussi pour un r�pertoire dans un jar ????****
          logger.info("Add a / at the end of a directory");
          url = url + "/";
        }
      } // c'est un r�pertoire
    } // �a n'est pas un jar
    logger.info("Tranformed URL: " + url);
    return new URL(url);
  }

  /**
   * Load the plugin instances from the URLs of the pluginManager and that have a type.
   * After, it is necessary to get the plugins with the
   * {@link #getPluginInstances} method.
   * @param type type of the wanted plugins.
   * <code>null</code> if one wants the plugin of any type.
   */
  public final void loadPlugins(Class type) {
    logger.info("loadPlugins(" + type
                + ") into urls " + urls);
    // 1. plugin loader loads the plugins
    pluginLoader.loadPlugins(type, true);
    // 2. Registers the loaded plugins
    pluginInstanceRegister.register(pluginLoader.getPluginInstances());
    logger.info("Plugins registered: " + pluginInstanceRegister);
  }

  /**
   * Load all the plugin instances.
   */
  public final void loadPlugins() {
    loadPlugins(null);
  }

  /**
   * Gets the plugin instances already loaded by the loadPlugins methods,
   * that has a certain type.
   * @param type type of the wanted plugins.
   * @return an array that contains the plugin instances.
   * Returns an array with length 0 if no instance
   * has been found. Returns null only in case of problem (exception or error).
   * Only one instance of each plugin class.
   * No null elements in the array.
   * If type is not null, the type of the array is "type"[] where "type"
   * is the parameter of the method. For example,
   * getPluginInstances(Drawer.class, o) returns a Drawer[].
   */
  public Plugin[] getPluginInstances(Class type) {
    return getPluginInstances(type, null);
  }

  /**
   * Gets all the plugin instances already loaded by the loadPlugins methods.
   * @return an array that contains the plugin instances.
   * Returns an array with length 0 if no instance
   * has been found. Returns null only in case of problem (exception or error).
   * Only one instance of each plugin class.
   * No null elements in the array.
   */
  public Plugin[] getPluginInstances() {
    return getPluginInstances(null, null);
  }

  /**
   * Gets all the plugin instances already loaded by the loadPlugins methods
   * that has a certain type and can process an object.
   * @param type type of the plugins.
   * @param object object that the plugins must be able to process.
   * @return plugin instances. Returns an array with length 0 if no instance
   * has been found. Returns null only in case of problem (exception or error).
   * Only one instance of each plugin class.
   * No null elements in the array.
   * If type is not null, the type of the array is "type"[]. For example,
   * getPluginInstances(Drawer.class, o) returns a Drawer[].
   */
  public Plugin[] getPluginInstances(Class type, Object object) {
    return pluginInstanceRegister.getPluginInstances(type, object);
  }

  /**
   * Gets one plugin instance already loaded by the loadPlugins methods
   * that has a certain type and can process an object.
   * @param type type of the plugins.
   * @param object object that the plugins must be able to process.
   * @return plugin instance. Returns an array with length 0 if no instance
   * has been found. Returns null only in case of problem (exception or error).
   */
  public Plugin getPluginInstance(Class type, Object object) {
    Plugin[] plugins = pluginInstanceRegister.getPluginInstances(type, object);
    if (plugins != null && plugins.length != 0) {
      return plugins[0];
    }
    else {
      return null;
    }
  }

  /**
   * Add a URL where to search the plugins.
   * Does not search them immediately.
   * A URL can point to:
   * <ul>
   *   <li> a .class file (which is a plugin)
   *   <li> a local directory (which contains plugins)
   *   <li> a jar file (which contains plugins).
   * </ul>
   * The URL of a jar or .class file can be a remote URL.
   * The URL will b registered only it is not already registered.
   * Formats of the URLs of the directories:
   * base directory of the class must be given, not the exact location of the
   * class files. For example, if the class fr.truc.Classe is located in
   * the directory /a/b/fr/truc/Classe.class, the URL must be
   * "/a/b/".
   * @param url URL to add.
   */
  public void addURL(URL url) {
    if (! urls.contains(url)) {
      urls.add(url);
    }
    pluginLoader.addURL(url);
  }

  /**
   * Add URLs where to search the plugins.
   * @param newUrls URLs where to search plugins. The array must be full.
   */
  public void addURL(URL[] newUrls) {
    for (int i = 0; i < newUrls.length; i++) {
      addURL(newUrls[i]);
    }
  }

  /**
   * Add URLs of all the jar placed in some local directories.<P>
   * Usage:
   * <pre>
   * addJarURLsInDirectories(new URL() { new URL("file:dir1/dir2"),
   *                                     new URL("file:dir2") });
   * </pre>
   * @param urls URLs of the directories which contain the jar files.
   */
  public void addJarURLsInDirectories(URL[] urls) {
    // Quickly added to help a user.
    // It will be difficult to remove all the jars afterwards!!! TO SEE

    // Verify that all the urls correspond to directories
    for (int i = 0; i < urls.length; i++) {
      URL url = urls[i];
      logger.info("***************** Directory " + url);
      if (url.getProtocol().equals("file")) { // ou file: ????
        File file = new File(url.getPath());
        if (file.isDirectory()) {
          // It is a directory ; keep all the files in it whose name
          // ends with ".jar"
          File[] jars = file.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
              return name.endsWith(".jar");
            }
          });
          // Add all the jars in this directory
          for (int j = 0; j < jars.length; j++) {
            try {
              logger.info("" + jars[j] + "-->" + jars[j].toString());
              addURL(jars[j].toString());
            }
            catch (MalformedURLException ex) {
              ex.printStackTrace();
            }
          }
        } // if (file.isDirectory())
      } // if (url.getProtocol().equals("file"))
    } // for
  }

  /**
   * Add URLs where to search the plugins.
   * @param url a string that contains a URL. Some syntax errors in the URL
   * will be corrected.
   * @throws MalformedURLException exception thrown if a syntax error has
   * not been corrected.
   */
  public void addURL(String url) throws MalformedURLException {
    addURL(getURL(url));
  }

  /**
   * Add URLs where to search the plugins.
   * @param urls
   * @throws MalformedURLException exception lanc�e si on ne peut deviner
   * comment corriger un URL mal form�.
   */
  public void addURL(String[] urls) throws MalformedURLException {
    for (int i = 0; i < urls.length; i++) {
      addURL(urls[i]);
    }
  }

  /**
   * Remove a URL for searching the plugins.
   * Plugins instances from this URL already got by clients can be used
   * by these clients, but they will not be returned yet by the
   * getPluginInstances instances.
   * @param url URL to remove.
   */
  public void removeURL(URL url) {
    /* PROBLEM: if all the plugin instances got from this url are removed,
     * other plugin instances not loaded from another url because they have
     * the same name as one of that plugin class will not be loaded.
     * Conclusion: I refreh and reload all the plugin instances (with the
     * current class loaders).
     */
    urls.remove(url);
    pluginLoader.removeURL(url);
    refresh();
    pluginInstanceRegister.unregisterPluginInstancesFrom(new URL[] {url});
  }

  /**
   * Reload all the plugins with their newest version.
   */
  public void refresh() {
//    logger.info("**** Create a new PluginLoader");
//    pluginLoader = new PluginLoader((URL[])urls.toArray(new URL[0]));
    pluginLoader.reloadPlugins();
    pluginInstanceRegister.clear();
    pluginInstanceRegister.register(pluginLoader.getPluginInstances());
  }

  /**
   * Reload all the plugins coming from a URL, with their newest version.
   * In the current version, reload <b>all</b> the plugins
   * and not only the plugins coming from the URL parameter.
   * @param url URL where to reload plugins.
   */
  public void refresh(URL url) {
    pluginLoader.reloadPlugins(url);
    pluginInstanceRegister.unregisterPluginInstancesFrom(new URL[] { url });
    pluginInstanceRegister.register(pluginLoader.getPluginInstances());
  }

  /**
   * Reload all the plugins coming from URLs, with their newest version.
   * In the current version, reload <b>all</b> the plugins
   * and not only the plugins coming from the URL parameter.
   * @param urls fully filled array containing the URLs.
   */
  public void refresh(URL[]  urls) {
    pluginLoader.reloadPlugins(urls);
    pluginInstanceRegister.unregisterPluginInstancesFrom(urls);
    pluginInstanceRegister.register(pluginLoader.getPluginInstances());
  }


  /**
   * Reload the plugin, with its newest version.
   * A appeler quand la classe du plugin a �t� mise � jour.
   * Attention, les plugins charg�s depuis le m�me URL passeront � une nouvelle
   * version si elle existe au prochain appel de loadPlugins.
   * NOT IMPLEMENTED.
   * @param instance instance du plugin � recharger.
   */
  public void refreshPlugin(Plugin instance) {
    throw new UnsupportedOperationException("TO DO...");
    // On recherche l'URL d'o� a �t� charg� ce plugin.
    // On met � null le chargeur pour cet URL.

    // On appelle la m�thode loadPlugin?????
    // A VOIR ?????
//    refresh();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer("PluginManager search in: ");
    for (Iterator iter = urls.iterator(); iter.hasNext(); ) {
       sb.append(iter.next() + ";");
    }
    return sb.toString();
  }

  public void addPluginManagerListener(PluginManagerListener listener) {

  }
}