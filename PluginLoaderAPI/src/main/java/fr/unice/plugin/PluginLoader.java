package fr.unice.plugin;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;

import java.util.logging.*;
import java.io.*;
import java.util.jar.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import static com.plarpebu.common.PlarpebuUtil.configureLogToFile;

/**
 * Load plugin classes from URLs, and create one instance of each class.
 * Delegate the loading to ClassLoaders.
 * One different classLoader by URL. All the plugins from one URL are loaded
 * by the same class loader. To reload new versions of plugins from a URL,
 * a new class loader is associated to this URL.
 * <P>
 * New plugin classes put in a URL can be loaded by loadPlugins methods.
 * In this case, new version of an already loaded class is not loaded.
 * New version of an already loaded class can be loaded by reloadPlugins
 * methods.
 * <P>
 * This class is used by PluginManager.
 * @author Richard Grin
 * @version 1.0
 */

class PluginLoader {
  /**
   * URLs where the plugins will be searched.
   * A key of the map is a URL and the value of that key is a class loader
   * which will search in the URL.
   */
  private Map urlInfos = new HashMap();
  /**
   * List which contain the information ({@link PluginInfo}) about
   * plugin instances already loaded by loadPlugins.
   * Information: the instance itself and the URL where it has been loaded.
   */
  private List loadedPluginInstances = new ArrayList();
  private String PLUGIN_LIST_FILENAME_IN_JAR = "pluginlist";

  /**
   * To cut all the messages, put in the main class
   * Logger logger = Logger.getLogger("fr.unice.plugin.PluginManager");
   * Logger.getLogger("fr.unice.plugin").setLevel(Level.SEVERE);
   * To get all the messages, put in the main class
   * Logger.getLogger("fr.unice.plugin").setLevel(Level.WARNING);
   */
  private static Logger logger = configureLogToFile(Logger.getLogger("fr.unice.plugin.PluginLoader"));

  /**
   * Creates an instance which will search the plugins in certain URLs.
   * @param urls tableau fully filled with URLs (no null elements).
   */
  PluginLoader(URL[] urls) {
    for (int i=0; i < urls.length; i++) {
      if (urls[i] == null) {
        throw new IllegalArgumentException("No null URL accepted !");
      }
      // the class loader will be created only when needed.
      this.urlInfos.put(urls[i], null);
    }
    logger.info("urls = " + urls + " Length = " + urls.length);
  }

  PluginLoader() {
  }

  /**
   * Add a URL for searching plugins.
   * Does nothing if the URL is already registered.
   * @param url � ajouter.
   */
  void addURL(URL url) {
    if (urlInfos.get(url) == null) {
      this.urlInfos.put(url, null);
    }
  }

  /**
   * Removes a URL for searching.
   * Does not remove instances already loaded by that URL.
   * C'est le manager de plugins qui demandera de le faire
   * au registre des instances de plugins.
   * @param url � enlever.
   */
  void removeURL(URL url) {
    this.urlInfos.remove(url);
  }

  /**
   * Reloads all the plugins.
   * Load new versions of the plugins if they are found.
   * @todo reload only if there is a modification since previous loading.
   */
  void reloadPlugins() {
    // All the class loaders will be set to null.
    eraseClassLoaders();
    // Erase all the plugin instances before loading new ones.
    loadPlugins(true);
  }

  /**
   * Loads all the plugins.
   * Load plugins from new classes but not the new versions.
   * of already loaded plugins.
   */
  void loadPlugins(boolean eraseAlreadyLoadedPlugins) {
    if (eraseAlreadyLoadedPlugins) {
      loadedPluginInstances.clear();
    }
    Iterator it = urlInfos.keySet().iterator();
    while (it.hasNext()) {
      // URL o� on va chercher les plugins
      loadPlugins(null, (URL)it.next());
    }
  }

  /**
   * Reload plugins from some URL. New versions are loaded.
   * @param url
   */
  void reloadPlugins(URL url) {
    // The class loader associated with the URL is erased;
    // so, a new class loader will be created and associated with the URL.
    urlInfos.put(url, null);
    loadPlugins(url);
  }

  /**
   * Reload plugins from some URLs. New versions are loaded.
   * @param urls
   */
  void reloadPlugins(URL[] urls) {
    for (int i = 0; i < urls.length; i++) {
      reloadPlugins(urls[i]);
    }
  }

  /**
   * Load plugins from some URL. New versions of already loaded plugins are
   * not loaded.
   * @param url
   * @todo reload only if there is a modification since previous loading.
   */
  void loadPlugins(URL url) {
    loadPlugins(null, url);
  }

  /**
   * Reload plugins of some type. New versions are loaded.
   * @param type
   * @todo reload only if there is a modification since previous loading.
   */
  void reloadPlugins(Class type) {
    // Met � null tous les chargeurs de classe
    eraseClassLoaders();

    // Pour avoir les derni�res versions on est oblig� de cr�er des
    // nouveaux chargeurs de classes pour toutes les URL.
    // On pourrait aussi faire chercher l'ancien chargeur de classes de l'URL et
    // ne cr�er un nouveau chargeur pour cet URL que si l'ancien a trouv�
    // des plugins du type....
    loadPlugins(type, true);
  }

  /**
   * Loads plugins of a certain type.
   * @param type type of the searched plugins. If null, loads all the types.
   * @param eraseAlreadyLoadedPlugins if true, first removes all the plugins
   * already loaded. So, new version of the plugins will be loaded.
   */
  void loadPlugins(Class type, boolean eraseAlreadyLoadedPlugins) {
    if (eraseAlreadyLoadedPlugins) {
      loadedPluginInstances.clear();
    }
    Iterator it = urlInfos.keySet().iterator();
    while (it.hasNext()) {
      // URL o� on va chercher les plugins
      loadPlugins(type, (URL)it.next());
    }
  }

  /**
   * Reload plugins from some URLs and of a certain type.
   * New versions are loaded.
   * Instances of the old version are still usable by those which have
   * their reference but will not be known by others.
   *
   * Attention, comme un nouveau chargeur de classe est cr�� pour cet URL,
   * toutes les nouvelles versions de tous les plugins de cet URL seront
   * charg�es lors des loadPlugins suivants (comme avec reloadPlugins).
   * @param type
   * @param url
   * @todo reload only if there is a modification since previous loading.
   */
  void reloadPlugins(Class type, URL url) {
    // PROBLEME : au prochain chargement des plugins de l'URL, toutes
    // les nouvelles versions seront charg�es, m�me si elles ne sont
    // pas de ce type car le chargeur de classe aura chang� !!!!
    // A VOIR *****

    // On efface le chargeur de classes qui s'occupait avant de cet URL ;
    // un nouveau chargeur sera ainsi cr�� lors de chargement.
    if (url != null) {
      urlInfos.put(url, null);
    }
    loadPlugins(type, url);
  }

  /**
   * Erase all the classLoaders associated with the URLs.
   */
  private void eraseClassLoaders() {
    Iterator it = urlInfos.keySet().iterator();
    while (it.hasNext()) {
      // URL o� on va chercher les plugins
      urlInfos.put(it.next(), null);
    }
  }

  /**
   * Charge une instance de chacun des plugins d'un certain type
   * plac�s dans un URL.
   * Ne charge que les nouveaux plugins mais pas les nouvelles versions
   * des plugins d�j� charg�s (voir {@link #reloadPlugins(Class) reloadPlugins}).
   * Conserve ces instances pour pouvoir les renvoyer par la m�thode
   * {{@link #getPluginInstances}}
   * Si l'URL n'est pas d�j� dans les URL enregistr�, on l'ajoute aux URL
   * du PluginLoader. Est-ce qu'il ne faudrait accepter que les URL d�j�
   * enregistr�s dans le PluginLoader ???
   *
   * Pour l'instant son appel
   * est limit� au paquetage ; ne devrais-je pas l'�tendre � protected ou
   * public ? A VOIR � l'usage...
   * @param type des plugins cherch�s. C'est une interface en pratique.
   * Charge tous les plugins si <code>null</code>.
   * @see #getPluginInstances
   */
  void loadPlugins(Class type, URL url) {
    // ATTENTION ! que faire si un plugin a d�j� �t� charg� ????
    // Dans cette version, on ne le recharge pas.

    // Charge toutes les classes susceptibles d'�tre des plugins
    // et ne retient que les plugins qui sont du type voulu.
    // Distingue les diff�rents types d'URL
    // - une seule classe distante ou non ; probl�me : on ne peut savoir le
    //   nom complet de la classe. Il faudrait donc le donner explicitement ;
    //   comment ? J'ai choisi d'imposer que la classe n'appartienne pas �
    //   un paquetage. A VOIR. Il doit �tre possible d'aller lire le nom
    //   du paquetage dans le fichier .class.
    // - un r�pertoire ** local ** ; attention, si le chargeur est dans un
    //   jar, le r�pertoire est aussi dans le m�me jar.
    // - un fichier jar distant ou non.

    // R�cup�re le chargeur de classe qui va charger les plugins
    ClassLoader cl = getClassLoader(url, false);
    logger.info("Gets the classLoader that loads from " + url);

    // D�termine si l'URL correspond � un jar, ou � un fichier .class
    // ou � un r�pertoire.
    String nomFichier = url.getFile();

    if (nomFichier.endsWith(".jar")) {
      // Compl�te l'URL si besoin est
      // S'il manque le "jar:" devant, on le rajoute
      // Normalement, �a ne devrait jamais arriver.
      // A VOIR....

      // Charge les plugins plac�s dans un jar.
      loadFromJar(url, type, cl);
    }
    else if (nomFichier.indexOf(".jar!") != -1) {
      // C'est un fichier jar mais avec un nom de r�pertoire qui contient
      // les plugins
      loadFromJar(url, type, cl);
    }
    else if (nomFichier.endsWith(".class")) {
      // Charge les plugins depuis un fichier .class.
      loadPluginsFromFile(url, type);
    }
    else {
      // �a doit �tre un r�pertoire
      /* Si le chargeur est dans un jar (�a sera le cas
       * si on livre une application qui utilise des plugins, dans un jar,
       * avec les plugins dans le m�me jar), le r�pertoire est aussi
       * dans ce jar.
       * Dans ce cas, la m�thode getUrl de PluginManager aura renvoy� un URL
       * qui commence par jar et on ne sera donc pas ici.
       */
      logger.info("url.getPath()=" + url.getPath());
      loadPluginsFromDirectory(url, type, cl);
    }
  }

  /**
   * Load plugins of a certain type from a directory which is not in a jar.
   * @param urlBase URL of the base directory; the class of a plugin in this
   * directory must be under this directory in a sub-directory which matches the
   * name of its package.
   * Example of a URL : file:rep/fichier
   * @param type type of the plugins. Load all the plugins if <code>null</code>.
   * @param cl the classLoader which will load the plugin.
   */
  private void loadPluginsFromDirectory(URL urlBase, Class type,
                                        ClassLoader cl) {
    // Pour trouver le nom complet des plugins trouv�s : c'est la partie
    // du chemin qui est en plus du r�pertoire de base donn� au loader.
    // Par exemple, si le chemin de base est rep1/rep2, le plugin
    // de nom machin.truc.P1 sera dans rep1/rep2/machin/truc/P1.class
    // Transforme les %20 en espaces (utile seulement pour Windows ?)
    String nomUrl = urlBase.getPath().replaceAll("%20", " ");
    File dir = new File(nomUrl);
    if (dir == null || ! dir.isDirectory()) {
      logger.warning("Le r�pertoire specifi� n'est pas correct : " + urlBase
                     +  " qui donne le fichier " + dir
                     + ". Il existe ? " + dir.exists());
      return;
    }

    // Si c'est un r�pertoire mais que l'URL ne se termine pas par un "/",
    // on ajoute un "/" � la fin (car URL ClassLoader oblige � donner
    // un URL qui se termine par un "/" pour les r�pertoires.
    // A FAIRE *********

    logger.info("=+=+=+=+=+ Entr�e dans loadPluginsFromDirectory=+=+=+=+");
    loadFromSubdirectory(dir.getPath(), dir, type, cl, urlBase);
    logger.info("=+=+=+=+=+ Sortie de loadPluginsFromDirectory=+=+=+=+");
  }

  /**
   * Loads the plugins placed directly in a subdirectory of a base directory.
   * Both directories are not in a jar.
   * @param baseName name of the base directory (to guess the name of the
   * du package of the found plugins).
   * @param dir subdirectory.
   * @param type type of the plugins to load.
   * @param urlBase URL to which the classLoader is associated (pour savoir d'o�
   * viennent les instances de plugins trouv�es ; � voir si on ne peut pas se
   * passer de ce param�tre...).
   * Charge tous les plugins si <code>null</code>.
   */
  private void loadFromSubdirectory(String baseName, File dir, Class type,
                                    ClassLoader cl, URL urlBase) {
    logger.info("Chargement dans le sous-r�pertoire " + dir
                + " avec nom de base " + baseName);
    // Le " + 1 " pour "inclure" un "/" final dans le nom du r�pertoire de base.
    int baseNameLength = baseName.length() + 1;
    // On parcourt toute l'arborescence � la recherche de classes
    // qui pourraient �tre des plugins.
    // Quand on l'a trouv�, on en d�duit son paquetage avec sa position
    // relativement � l'URL de recherche.
    File[] files = dir.listFiles();
    logger.info("Le listing : " + files);
    // On trie pour que les plugins apparaissent dans le menu
    // par ordre alphab�tique
    //    Arrays.sort(list);
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      if (file.isDirectory()) {
        // it is a directory
        loadFromSubdirectory(baseName, file, type, cl, urlBase);
        continue;
      }
      // it is not a directory
      logger.info("Examen du fichier " + file.getPath() + ";" + file.getName());
      String path = file.getPath();
      String qualifiedClassName = getQualifiedName(baseNameLength, path);
      // On obtient une instance de cette classe
      if (qualifiedClassName != null) {
        Plugin plugin = getInstance(qualifiedClassName, type, cl);
        if(plugin != null) {
          logger.info("Classe " + qualifiedClassName + " est bien un plugin !");
          // To add the last modication time of the file
//          long lastModificationTime = file.lastModified();
//          loadedPluginInstances.add(
//              new PluginInfo(plugin, urlBase, lastModificationTime));
          loadedPluginInstances.add(new PluginInfo(plugin, urlBase));
          logger.info("Les plugins : " + loadedPluginInstances);
        }
      }
    } // for
  }

  /**
   * Recherche les plugins plac�s dans un r�pertoire d'un fichier jar.
   * Cette m�thode est utilis�e quand l'application est dans un jar et que
   * l'on cherche les plugins dans le m�me r�pertoire que l'application.
   * @param url URL du r�pertoire du jar dans lequel on cherche les plugins.
   * @param cheminRep r�pertoire du jar dans lequel on cherche les plugins
   * (relatif � la racine du jar). Le nom de paquetage du plugin devra
   * correspondre � son chemin dans le jar par rapport � ce r�pertoire.
   * Par exemple, si cheminRep est repplugins, le plugin de nom complet
   * fr.unice.DessinateurDeCarres devra se trouver dans le r�pertoire du jar
   * repplugins/fr/unice.
   * @param type type des plugins que l'on cherche.
   * Charge tous les plugins si <code>null</code>.
   * @param cl chargeur de classes qui va charger les plugins
   */
//  private void loadPluginsFromDirectoryInSameJar(URL url,
//                                                 String cheminRep,
//                                                 Class type,
//                                                 ClassLoader cl) {
//    /* Pour fixer les id�es : on part d'un url �gal �
//     * jar:/D:/rep1/rep2/f.jar!/rep3/plugins
//     * Ne se termine pas par un "/" ?? A voir pendant l'ex�cution avec le logger
//     * et d'un cheminRep �gal � *******
//     */
//    logger.info("Chargement de plugins dans le r�pertoire " +
//                cheminRep + " du jar ";
//                url.getFile() + " du jar " + url);
//
//    // Sera utilis� pour trouver le nom complet des classes
//    int baseNameLength = cheminRep.length();
//
//    // Pour trouver le nom complet des plugins trouv�s : c'est le chemin
//    // relatif par rapport au r�pertoire dans le fichier jar.
//    // Par exemple, le plugin machin.truc.P1
//    // sera dans le fichier plugins/machin/truc/P1.class du jar si
//    // cheminRep est plugins.
//
//    // Extraction du nom du fichier jar, et du r�pertoire dans le jar.
//    // file contient une String du type
//    // file:/D:/Fac/TP plugins/plugin.jar!/plugins
//    // ou http://deptinfo.unice.fr/rep/plugin.jar!/plugins
//    // ou http://deptinfo.unice.fr/rep/plugin.jar!/ si les plugins
//    // sont plac�s directement sous la racine du jar.
//    int n = fileName.indexOf("!");
//    String urlFichierJar = fileName.substring(0, n);
//    logger.info("Examen du jar " + urlFichierJar);
//    String nomRepPlugins = fileName.substring(n + 2);
//    logger.info("R�pertoire dans le jar : " + nomRepPlugins);
//
//    // R�cup�re les entr�es du fichier jar qui sont dans le r�pertoire
//    try {
//      JarURLConnection jc = (JarURLConnection)url.openConnection();
//      logger.info("Ouverture connexion sur jar " + url.getPath());
//      JarFile jf = jc.getJarFile();
//      ArrayList classesPlugins = new ArrayList();
//      Enumeration e = jf.entries();
//      while (e.hasMoreElements()) {
//        JarEntry je = (JarEntry)e.nextElement();
//        String nomJe = je.getName();
//        // On ne garde que les entr�es qui sont sous le r�pertoire rep.
//        // On ne veut pas le r�pertoire lui-m�me.
//        if (nomJe.startsWith(cheminRep) && ! nomJe.equals(cheminRep)) {
//          logger.info("Fichier " + nomJe + " trouv� dans " + cheminRep);
//          // Le plugin doit �tre sous le r�pertoire.
//          // Il est plac� dans un r�pertoire qui correspond � son paquetage.
//          // On va r�cup�rer le nom du paquetage d'apr�s sa position dans
//          // le fichier jar. Il faudra alors v�rifier que �a correspond bien.
//
//          // On r�cup�re le chemin relatif au r�pertoire de recherche
//          // (qui correspond au nom du paquetage de la classe).
//          String finNom = nomJe.substring(cheminRep.length() + 1);
//          logger.info("Nom relatif du fichier par rapport � "
//                      + cheminRep + " = " + finNom);
//          // Le nom doit se terminer par .class
//          // et ne pas contenir un "$" (pas une classe interne)
//          //          if (finNom.indexOf('$') == -1 & finNom.endsWith(".class")) {
//          // Ca correspond bien � une classe de plugin
//          // On construit le nom de la classe en enlevant le .class
//          // et en rempla�ant les "/" par des "."
//          // On commence par retirer le r�pertoire de recherche
//          // du d�but du nom
//
//            String qualifiedClassName = getQualifiedName(baseNameLength, nomJe);
//            if (qualifiedClassName == null) {
//              // Ca n'est pas une classe externe.
//              continue;
//            }
//
//            //            int n = nomJe.lastIndexOf(".class");
//            //            nomJe = nomJe.substring(0, n);
//            //            String nomClasse = nomJe.replace('/', '.');
//
//            logger.info("Classe " + qualifiedClassName + " trouv�e dans le jar.");
//
//            Plugin plugin = getInstance(qualifiedClassName, type, cl);
//            if (plugin != null) {
//              logger.info("Classe " + qualifiedClassName + " est bien un plugin !");
//              loadedPluginInstances.add(new PluginInfo(plugin, url));
//            }
//        }
//      } // while (e.hasMoreElements())
//
//      // Pour trier, le plus simple est de cr�er une classe abstraite
//      // AbstractPlugin qui impl�mente Comparable en comparant les noms
//      // des plugins. Laiss� en exercice.
//      // Collections.sort(plugins);
//    }
//    //      catch(MalformedURLException err) {
//    //        err.printStackTrace();
//    //      }
//    catch (IOException err) {
//      err.printStackTrace();
//    }
//  }

  /**
   * Loads the plugins of a certain type in the directory of a jar.
   * Conserve une instance de chacun des plugins charg�s.
   * Si on ne donne pas un r�pertoire particulier dans le jar, on cherche
   * si le jar contient une entr�e "pluginlist" qui donnerait la liste
   * de tous les fichiers plugins du jar.
   * Les plugins doivent se trouver dans le r�pertoire du jar qui correspond
   * au nom de leur paquetage. Par exemple, la classe
   * fr.unice.DessinateurDeCarre doit se trouver dans le r�pertoire
   * fr/unice/DessinateurDeCarre du jar.
   * @param url URL of the jar in which the plugins will be loaded.
   * If this URL contains a directory, the plugins will be searched only in
   * this directory of the jar.
   * Example : jar:file:plugins.jar!/rep1/rep2
   * @param type of the plugins.
   */
  private void loadPluginsFromJar(URL url, Class type, ClassLoader cl) {
    /* A VOIR : faut-il ajouter la possibilit� que les plugins soient dans un
    * des r�pertoires du jar, que l'on indiquerait dans l'URL, par exemple,
    * file:/D:/Fac/TP plugins/plugin.jar!/plugins (par le r�pertoire plugins
    * du fichier jar) ? Est-ce que �a a un int�r�t ?
    * Dans cette version, je le mets mais je ne le donne pas dans la doc.
    */
    // On construit le nom complet du fichier pour qu'il soit portable
    // L'API java renvoie toujours des noms avec "/" comme s�parateur.
    // Il ne faut donc pas utiliser la propri�t� file.separator.
    String nomFichierListePlugin = "META-INF" + "/"
                                   + PLUGIN_LIST_FILENAME_IN_JAR;

    // 2 cas :
    //   - si le jar contient un fichier META-INF/pluginlist
    //     on lit les noms des plugins dans ce fichier ;
    //   - sinon, on recherche dans tout le jar les �ventuels plugins.

    // Pour une prochaine it�ration : le fichier qui contient la liste des
    // plugins est un fichier xml qui contient des entr�es pour tous les
    // plugins. Par exemple :
    // <plugin>
    //   <name>DessinateurChirac</name>
    //   <location>rep1/rep2/DessinateurChirac</location>
    //   <description> Bla bla bla </description>
    // </plugin>
    // <plugin>
    // <name>DessinateurJospin</name>
    // ....

    /* Dans le cas o� on n'utilise pas META-INF/pluginlist, voici comment
     * on trouve le nom complet de la classe des plugins trouv�s :
     * c'est le chemin relatif par rapport au r�pertoire dans le fichier jar.
     * Par exemple, le plugin machin.truc.P1
     * sera dans le fichier plugins/machin/truc/P1.class du jar si le
     * r�pertoire des plugins est plugins.
     * Il sera dans l'entr�e du jar machin/truc/P1.class si on ne donne
     * pas de r�pertoire particulier dans le jar.
     */

    // Extraction du nom du fichier jar, et du r�pertoire dans le jar.
    String fileName = url.getFile();
    // file contient une String du type
    // file:/D:/Fac/TP plugins/plugin.jar!/plugins
    // ou http://deptinfo.unice.fr/rep/plugin.jar!/plugins
    // ou http://deptinfo.unice.fr/rep/plugin.jar!/ si les plugins
    // sont plac�s directement sous la racine du jar.
    int n = fileName.indexOf("!");
    String urlFichierJar;
    String nomRepPlugins;
    if (n == -1) {
      // pas de r�pertoire indiqu� dans le jar
      urlFichierJar = fileName;
      nomRepPlugins = "/"; // ??????******
    }
    else {
      urlFichierJar = fileName.substring(0, n);
      nomRepPlugins = fileName.substring(n + 2);
    }
    logger.info("Searches in the jar " + urlFichierJar);
    logger.info("Directory in the jar : " + nomRepPlugins);

    // R�cup�re les entr�es du fichier jar qui sont dans le r�pertoire
    JarFile jf = null;
    try {
      logger.info("Open connection of the jar " + url);
      JarURLConnection jc = (JarURLConnection)url.openConnection();
      jf = jc.getJarFile();
      // On recherche si le jar contient le fichier META-INF/pluginslist
      // Ce fichier contient les noms de tous les plugins contenus
      // dans le fichier jar.
      // Comme certains outils zip sous Windows transforment les casses
      // des caract�res, on prend ses pr�cautions !
      // Si on se moque de ce probl�me de casse, on peut faire comme ceci :
      // JarEntry entry = getJarEntry("META-INF/pluginslist");
      // (renvoie null si l'entr�e n'existe pas).
      JarEntry je = null;
      boolean pluginListFound = false;
      Enumeration entries = jf.entries();
      while (entries.hasMoreElements()) {
        je = (JarEntry)entries.nextElement();
        logger.info("Entr�e de jar : " + je);

        if (je.getName().equalsIgnoreCase(nomFichierListePlugin)) {
          pluginListFound = true;
          break;
        }
      }
      if (pluginListFound) {
        // On a trouv� la liste des plugins.
        // On charge tous les plugins dont le nom est dans cette liste
        // et qui sont plac�s dans ce fichier jar.
        logger.info("pluginlist file found");
        loadPluginsInListOfJar(je, jf, type, cl, url);
      }
      else {
        logger.info("pluginlist file not found");
        // On n'a pas trouv� la liste des plugins.
        // On essaie tous les fichiers du jar pour voir si ce sont des plugins.
        // On fini de parcourir toutes les entr�es du jar avant de voir
        // lesquelles sont des plugins.
        entries = jf.entries();
        while (entries.hasMoreElements()) {
          je = (JarEntry)entries.nextElement();
          String nomJe = je.getName();
          if (nomJe.startsWith(nomRepPlugins)) {
            logger.info("Fichier " + nomJe + " trouv� dans "
                        + nomRepPlugins);
            // Le plugin doit �tre sous le r�pertoire.
            // Il est plac� dans un r�pertoire qui correspond � son paquetage.
            // On va r�cup�rer le nom du paquetage d'apr�s sa position dans
            // le fichier jar. Il faudra alors v�rifier que �a correspond bien.

            // Le nom doit se terminer par .class
            // et ne pas contenir un "$" (pas une classe interne)
            String finNom = nomJe.substring(nomRepPlugins.length());
            if (finNom.indexOf('$') == -1 && finNom.endsWith(".class")) {
              // Ca correspond bien � une classe de plugin
              // On construit le nom de la classe en enlevant le .class
              // et en rempla�ant les "/" par des "."
              n = finNom.lastIndexOf(".class");
              finNom = finNom.substring(0, n);
              String nomClasse = finNom.replace('/', '.');

              logger.info("Classe " + nomClasse + " trouv�e dans le jar.");

              Plugin plugin = getInstance(nomClasse, type, cl);
              if (plugin != null) {
                logger.info("Classe " + nomClasse + " est bien un plugin !");
                loadedPluginInstances.add(new PluginInfo(plugin, url));
              } // if (plugin != null)
            } // if (finNom ne contient pas de $ et se termine par .class)
          } // if nomJe est dans le bon r�pertoire
        } // while (e.hasMoreElements())
      } // else (pas de liste de plugins dans le jar)
    } // try
    catch (IOException err) {
      err.printStackTrace();
    }
    finally {
      /* Il ne faut pas fermer le jar ici sinon on ne pourra recharger les plugins
       * ou r�cup�rer des images ou d'autres ressources.
       * Il faudra bien pourtant le fermer. O� le faire ? A VOIR *********
      if (jf != null) {
        try {
          jf.close();
        }
        catch(IOException ex) {
          ex.printStackTrace();
        }
      }
      */
    } // finally
  } // M�thode loadPluginsFromJar

  /**
   * Loads the plugins that matches a name in the file "pluginlist" of a jar
   * (one name of plugin by line in this file).
   * A name can be the name of a directory of the jar in which the plugins
   * will be searched, or the name of a .class file (a plugin class).
   * If it is a .class file, the name must correspond to the package of the
   * class.
   * (si on avait un fichier XML comme fichier
   * de la liste des plugins, on pourrait donner le nom du paquetage de la
   * classe et on n'aurait pas alors cette contrainte pour mettre un .class
   * dans le jar).
   * @param je jar entry which contains the locations of the plugins
   * (directory of .class file).
   * @param jf jar file in which the plugins are searched.
   * @param type type of the plugins to load.
   * @param cl classLoader which will load the plugins.
   * @param urlJar URL of the jar in which the plugins are searched.
   */
  private void loadPluginsInListOfJar(JarEntry je, JarFile jf, Class type,
                                      ClassLoader cl, URL urlJar)
      throws IOException {
    logger.info("*****Start loadPluginsInListOfJar(" + je + ", "
                + jf + ", " + type + ", "
                + cl + ", " + urlJar + ")");
    // Lecture ligne � ligne de l'entr�e
    BufferedReader br =
        new BufferedReader(new InputStreamReader(jf.getInputStream(je)));
    // Nom de fichier du jar dans lequel on va chercher les plugins.
    String fileName;
    while ((fileName = br.readLine()) != null) {
      // Le nom de plugin est soit le nom d'un r�pertoire (nom relatif
      // � la racine du jar) dans lequel on va chercher les plugins,
      // ou le nom d'un fichier .class du jar.
      if (fileName.endsWith(".class")) {
        fileName = fileName.substring(0, fileName.length() - 6);
        logger.info("Chargement de " + fileName);
        // Cas o� le plugin est donn� par son fichier .class
        // On demande au chargeur de classe de le charger.
        // En effet, le chargeur de classe cherche dans ce jar
        // et il suffit donc de lui donner le nom de la classe.
        Plugin plugin = getInstance(fileName, type, cl);
        if (plugin != null) {
          logger.info("Classe " + fileName + " est bien un plugin !");
          loadedPluginInstances.add(new PluginInfo(plugin, urlJar));
        } // if plugin != null
      } // if nom se termine par .class
      else { // On suppose que fileName est un nom de r�pertoire du jar
        // On recherche les plugins dans ce r�pertoire du jar
        logger.info("**Les plugins sont dans le r�pertoire " + fileName);
        // Si le nom de r�pertoire ne se termine pas par un "/", on l'ajoute
        if (! fileName.endsWith("/")) {
          fileName += "/";
        }
        // Recherche les plugins dans le jar sous le r�pertoire
        loadFromJarWithDirectory(urlJar, fileName, type, cl);
      } // else (it is a directory)
    } // while
  } // m�thod loadPluginsInListOfJar

  /**
   * For the path of a .class file,
   * compute the qualified name of a class from the name of a base directory
   * and the path of the class, both paths being anchored at the same root
   * directory.
   * The base directory ends with "/" (see URLClassLoader class).
   * For example, a/b/c/ (6 for baseNameLength) and a/b/c/d/e/F.class
   * will return d.e.F
   * @param baseNameLength nombre de caract�res du nom du r�pertoire de base.
   * @param classPath chemin de la classe.
   * @return qualified name of the class, or null the name does not correspond
   * to an extern class.
   */
  private String getQualifiedName(int baseNameLength, String classPath) {
    logger.info("Compute the qualified name " + classPath + " by removing "
                + baseNameLength + " characters at the beginning");
    // Un plugin ne peut �tre une classe interne
    if ((! classPath.endsWith(".class")) || (classPath.indexOf('$') != -1)) {
      return null;
    }
    // C'est bien une classe externe
    // On retire du d�but le baseName et on remplace les "/" par des "."
    // Pourquoi + 1 ???????????????? Corrig�....
    // Ca marche pour la recherche dans un r�pertoire hors d'un jar
    // mais pas s'il est dans un jar. A VOIR !!!!!!!!
    classPath = classPath.substring(baseNameLength)
                       .replace(File.separatorChar, '.');
    // On enl�ve le .class final pour avoir le nom de la classe
    logger.info("Nom complet de la classe : " + classPath);
    return classPath.substring(0, classPath.lastIndexOf('.'));
  }

  /**
   * Charge un plugin plac� dans un fichier .class
   * @param url URL du fichier .class.
   * @param type type des plugins que l'on cherche.
   * Charge tous les plugins si <code>null</code>.
   * @todo TO IMPLEMENT
   */
  private void loadPluginsFromFile(URL url, Class type) {
    // Quel classloader utiliser ? Si on utilise un URLClassLoader, on va
    // �tre oblig� d'ajouter le r�pertoire de url comme chemin pour
    // cet URLClassLoader et on pourrait alors charger des plugins non
    // souhait�s.
    // Je pense utiliser un URLClassLoader sp�cial juste pour charger cette
    // classe et r�cup�rer une instance du plugin. Ainsi ce CL ne chargera pas
    // autre chose.
    // De toute fa�on, il semble que je serai amen� � utiliser un CL diff�rent
    // pour chaque URL. Sinon, on risque de charger des plugins ind�sirables
    // � la place d'un autre plugin de m�me nom plac� dans un autre URL.
    // Pour cette 1�re it�ration, j'accepte cette impr�cision et je n'impl�mente
    // pas cette m�thode.....

  }

  /**
   * Returns a plugin instance of a given type and class name.
   * @param nomClasse name of the plugin class.
   * @param type type of the plugin
   * @param cl class loader which will load the plugin.
   * @return a plugin instance. Returns null if there is a problem,
   * for example, if the plugin has not the good type.
   */
  private Plugin getInstance(String nomClasse, Class type, ClassLoader cl) {
    try {
      // C'est ici que se passe le chargement de la classe par le
      // chargeur de classes.
      logger.info("Loading of the class " + nomClasse + " by " + this
                  + " which search in the URLs " + this.urlInfos);
      Class c = cl.loadClass(nomClasse);
      Plugin plugin = null;
      try {
        // On cr�e une instance de la classe
        plugin = (Plugin)c.newInstance();
      }
      catch (ClassCastException e) {
        //Le fichier  n'est pas un plugin 'Plugin'
        logger.warning("Class " + nomClasse + " is not a plugin");
        return null;
      }
      catch (InstantiationException e ) {
        logger.info("Class " + nomClasse + " cannot be instancied");
        return null;
      }
      catch (IllegalAccessException e ) {
        logger.warning("No access to the class " + nomClasse);
        return null;
      }
      catch (NoClassDefFoundError e ) {
        logger.warning("Error while defining " + nomClasse + " (name of the package?");
        return null;
      } // Fin des catchs pour newInstance

      // Tests if the plugin has the good type
      if (type == null || type.isInstance(plugin)) {
        logger.info("Plugin with the name " + plugin.getName() + " found.");
        return plugin;
      }
      else {
        logger.info("Plugin with the name " + plugin.getName()
                    + " has not the good type " + type.getName());
        return null;
      }

    } catch (ClassNotFoundException e) {
      logger.warning("Class " + nomClasse + " cannot be found");
      return null;
    }
    catch (NoClassDefFoundError e ) {
      logger.warning("Error while defining " + nomClasse + " (name of the package?");
      return null;
    } // Fin des catchs pour loadClass
  }

  /**
   * Loads a plugin from a jar.
   * <P>
   * Dans l'URL on peut donner un nom de r�pertoire du jar (ou m�me un nom de
   * .class ?) (par exemple, jar:file:rep/truc.jar!/rep/plugins).
   * En ce cas, les plugins sont cherch�s directement sous ce r�pertoire.
   * <P>
   * Si on ne donne pas de nom de r�pertoire (par exemple, un URL du type
   * jar:file:rep/truc.jar!/, on regarde si le jar contient une entr�e
   * pluginlist.
   * Si c'est le cas, on cherche les plugins dans les noms
   * contenus dans pluginlist (une entr�e par ligne).
   * Sinon on recherche les plugins dans tout le jar.
   * <P>
   * L'emplacement du plugin doit correspondre � son nom complet (avec
   * le paquetage). Si un r�pertoire a �t� donn�, c'est l'emplacement par
   * rapport au r�pertoire qui doit correspondre. Sinon, c'est l'emplacement
   * par rapport � la racine du jar.
   *
   * @param url url o� on cherche les plugins
   * @param type type des plugins � r�cup�rer
   * @param cl chargeur de classes qui charge les classes des plugins
   */
  private void loadFromJar(URL url, Class type, ClassLoader cl) {
    /* Est-ce que l'URL contient un r�pertoire ?
     * Si oui, appeler la m�thode loadFromJarWithDirectory.
     * Sinon, appeler la m�thode loadFromJarWithoutDirectory.
     */
    String nomUrl = url.getPath();
    int n = nomUrl.indexOf("!/") + 2;
    if (n == nomUrl.length()) {
      loadFromJarWithoutDirectory(url, type, cl);
    }
    else {
      // On extrait le nom du r�pertoire
      String cheminRep = nomUrl.substring(n);
      loadFromJarWithDirectory(url, cheminRep, type, cl);
    }

  }

  /**
   * Loads a plugin from a directory of a jar. Pour chaque plugin
   * charg�, une instance est rang�e dans loadedPluginInstances
   * @param url URL du jar
   * @param cheminRep nom du r�pertoire du jar dans lequel on cherche les
   * plugins. Ne commence pas par un "/" (par exemple, rep/repplugins).
   * Le s�parateur est "/".
   * @param type type des plugins que l'on charge
   * @param cl chargeur de classes qui charge la classe des plugins.
   */
  private void loadFromJarWithDirectory(URL url, String cheminRep,
                                        Class type, ClassLoader cl) {
    logger.info("loadFromJarWithDirectory : URL du jar dans lequel on cherche : "
                + url);
    logger.info("R�pertoire du jar dans lequel on cherche : " + cheminRep);
    int lCheminRep = cheminRep.length();
    logger.info("Longueur chemin � retirer au d�but des noms d'entr�es du jar = "
                + lCheminRep);

    JarFile jf = null;
    try {
      logger.info("Ouverture connexion sur jar " + url);
      JarURLConnection jc = (JarURLConnection)url.openConnection();
      jf = jc.getJarFile();
      JarEntry je = null;
      boolean pluginListFound = false;
      Enumeration entries = jf.entries();
      while (entries.hasMoreElements()) {
        je = (JarEntry)entries.nextElement();
        String nomJe = je.getName();
        if (nomJe.startsWith(cheminRep)) {
          // On a trouv� une entr�e plac�e dans le bon r�pertoire
          logger.info("Fichier " + nomJe + " trouv� dans " + cheminRep);
          /* Le plugin doit �tre sous le r�pertoire qui correspond � son
           * paquetage.
           * On va donc r�cup�rer le nom du paquetage d'apr�s sa position dans
           * le fichier jar. Il faudra alors v�rifier que �a correspond bien.
           * On commence par v�rifier que l'entr�e se finit par .class et
           * qu'elle ne contient pas de $ (pas une classe interne).
           */
          /* Si l'entr�e est celle du r�pertoire cheminRep, �a ne nous
           * int�resse pas.
           */
          if (nomJe.length() == lCheminRep) {
            continue;
          }
          String finNom = nomJe.substring(lCheminRep + 1);
          if (finNom.indexOf('$') == -1 && finNom.endsWith(".class")) {
            /* Ca peut correspondre � une classe de plugin
             * On construit le nom de la classe en enlevant le .class
             * et en rempla�ant les "/" par des "."
             */
            int n = nomJe.lastIndexOf(".class");
            nomJe = nomJe.substring(0, n);
            String nomClasse = nomJe.replace('/', '.');
            // Il faut enlever le r�pertoire de base devant le nom de l'entr�e
            nomClasse = nomClasse.substring(lCheminRep);

            logger.info("Classe " + nomClasse + " trouv�e dans le jar.");

            Plugin plugin = getInstance(nomClasse, type, cl);
            if (plugin != null) {
              logger.info("Classe " + nomClasse + " est bien un plugin !");
              // Add last modification time of the entry in the pluginInfo
//              long lastModificationTime = je.getTime();
//              loadedPluginInstances.add(
//                  new PluginInfo(plugin, url, lastModificationTime));
              loadedPluginInstances.add(new PluginInfo(plugin, url));
            } // if (plugin != null)
          } // if (finNom ne contient pas de $ et se termine par .class)
        } // if nomJe est dans le bon r�pertoire
      } // while (e.hasMoreElements())
    } // try
    catch (IOException err) {
      err.printStackTrace();
    }
    finally {
      /* Jar must not be closed here else it will not be possible to reload
       * plugins or get images or other resources.
       */
//      if (jf != null) {
//        try {
//          jf.close();
//        }
//        catch(IOException ex) {
//          ex.printStackTrace();
//        }
//      }
    } // finally
  } // loadFromJarWithDirectory method

  /**
   * Load plugins in a jar, without specifying a directory.
   * 2 cases :
   *   - if an entry "META-INF/pluginlist" of the jar contains locations for plugins,
   *     the plugins will be searched in these locations;
   *   - else, plugins will be searched everywhere in the jar.
   * For each loaded plugin, an instance is put in loadedPluginInstances
   * @param url URL du jar
   * @param type type of the plugins wanted
   * @param cl class loader which load the plugin class.
   */
  private void loadFromJarWithoutDirectory(URL url, Class type,
                                           ClassLoader cl) {
    String nomTerminalFichierListePlugin = "pluginlist";
    /* Don't use the property file.separator because "/" is always used. */
    String nomFichierListePlugin = "META-INF/"
                                 + PLUGIN_LIST_FILENAME_IN_JAR;;

    /* 2 cases:
     *   - if the jar contains a file META-INF/pluginlist
     *     the path of the plugins are read in this file;
     *   - else, plugins are searched throughout the jar.
     */

    /* For a next iteration: the file that contains the path of the plugins
     * is an xml file. For example:
     * <plugin>
     *   <qualifiedname>fr.unice.plugindessinateur.DessinateurChirac</qualifiedname>
     *   <location>rep1/rep2/DessinateurChirac</location>
     *   <description> Blah blah blah </description>
     * </plugin>
     * <plugin>
     * <name>DessinateurJospin</name>
     * ....
     */

    /* In case META-INF/pluginlist is not found,
    * the qualified name of the plugin class found is guessed:
    * it is the path relative to the directory in the jar.
    * For example, the plugin machin.truc.P1
    * will be in the entry plugins/machin/truc/P1.class of the jar if the
    * plugins directory is "plugins".
    * It will be in the jar entry machin/truc/P1.class if there is no
    * directory given in the jar.
    */

    /* Because some zip tools of Windows sometimes modify characters
     * from lowercase to uppercase, we are cautious!
     * If it is not a problem, it is simpler to write:
     * JarEntry entry = getJarEntry("META-INF/pluginslist");
     * (returns null if the entry does not exist).
     */
    // Gets the jar entries in the url
    JarFile jf = null;
    try {
      logger.info("Open a connection with jar " + url);
      JarURLConnection jc = (JarURLConnection)url.openConnection();
      jf = jc.getJarFile();
      JarEntry je = null;
      boolean pluginListFound = false;
      Enumeration entries = jf.entries();
      while (entries.hasMoreElements()) {
        je = (JarEntry)entries.nextElement();
        logger.info("Entry of jar : " + je);
        if (je.getName().equalsIgnoreCase(nomFichierListePlugin)) {
          pluginListFound = true;
          break;
        }
      }
      if (pluginListFound) {
        // List of plugins file found.
        // All the plugins in that list are loaded.
        logger.info("pluginlist found");
        loadPluginsInListOfJar(je, jf, type, cl, url);
      }
      else {
        /* List of plugins file not found.
         * Plugins are searched in the jar, with "" as the base directory.
         */
        logger.info("pluginlist not found");
        loadFromJarWithDirectory(url, "", type, cl);
      } // else (List of plugins file not found)
    } // try
    catch (IOException err) {
      err.printStackTrace();
    }
    finally {
      /* Don't close the jar here, else it will not be possible to load any
       * resources from the jar.
       */

//      if (jf != null) {
//        try {
//          jf.close();
//        }
//        catch(IOException ex) {
//          ex.printStackTrace();
//        }
//      }
    }
  } // M�thode loadFromJarWithoutDirectory


  // Faut-il red�finir loadClass pour ne renvoyer que des plugins ????

  /**
   * Returns the plugin instances got until now (since the last erasure of the
   * plugins during a previous search {@link #loadPlugins(boolean)}.
   * @return plugin instances got until now. The array is full (no null entry).
   */
  PluginInfo[] getPluginInstances() {
    return (PluginInfo[])loadedPluginInstances.toArray(new PluginInfo[0]);
  }

  /**
   * Returns a class loader associated with a URL.
   * Concentrates the rules of creation of the class loaders.
   * @param url which will be associated with the returned class loader.
   * @param newClassLoader true if a new class loader is wanted;
   * false if the current class loader associated with the URL is wanted.
   * @return a classloader associated to the url parameter, or null in case
   * of a problem (no problem in the current version).
   */
  private ClassLoader getClassLoader(URL url, boolean newClassLoader) {
    ClassLoader cl;
    if (newClassLoader) {
      // return a new class loader
      cl = URLClassLoader.newInstance(new URL[] {url});
      // it is put in the map which contains the class loader for each URL
      urlInfos.put(url, cl);
    }
    else { // get the current class loader for url
      cl = (ClassLoader)urlInfos.get(url);
      if (cl == null) {
        // if there is no class loader for url, a new one is created
        cl = URLClassLoader.newInstance(new URL[] {url});
        // it is put in the map which contains the class loader for each URL
        urlInfos.put(url, cl);
      }
    }
    return cl;
  }

  public String toString() {
    return "PluginLoader charge depuis" + urlInfos;
  }
}