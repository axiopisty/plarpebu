package fr.unice.grin.detectjar;

import java.net.URL;

/**
 * Classe utilitaire qui permet de savoir si la classe principale d'une
 * application est dans un jar ou non.
 *
 * @author Richard Grin
 * @version 1.0
 */
public class DetectJar {

  /**
   * M�thode pour tester : affiche toutes les classes plac�es dans la pile
   * d'ex�cution.
   * On voit que la m�thode main est la derni�re m�thode de la pile.
   *
   * @return l'URL de la m�thode main de l'application
   */
  public static URL printClassesInStackTrace() {
    /* Id�e : on cr�e une exception et on va travailler dans la trace
     * pour trouver le nom de la classe de la m�thode main.
     * Ensuite on recherche cette classe comme une ressource.
     * Le r�sultat de getResource donne un URL. On regarde si cet URL
     * correspond � un jar ou non.
     */
    String classePrincipale = "";
    Throwable t = new Throwable();
    StackTraceElement[] elements = t.getStackTrace();
    for(int i = 0; i < elements.length; i++) {
      String methode = elements[i].getMethodName();
      String classe = elements[i].getClassName();
      System.out.println("Classe " + i + " : " + classe);
      System.out.println("M�thode " + i + " : " + methode);
      if(methode.equals("main")) {
        classePrincipale = classe;
      }
    }
    // Recherche de la classe comme ressource
    // On transforme d'abord le nom de la classe en chemin de ressource
    // � partir du classpath :
    String nomRessource = classePrincipale.replace('.', '/');
    nomRessource = "/" + nomRessource + ".class";
    System.out.println("nom de la classe comme ressource = " + nomRessource);
    URL urlClassePrincipale = null;
    try {
      urlClassePrincipale = Class.forName(classePrincipale).getResource(nomRessource);
      System.out.println("urlClassePrincipale = " + urlClassePrincipale);
    } catch(ClassNotFoundException e) {
    }
    System.out.println("Protocole = " + urlClassePrincipale.getProtocol());
    return urlClassePrincipale;

  }

  /**
   * D�tecte si la classe principale de l'application est dans un jar ou non.
   *
   * @return l'URL de la classe principale de l'application.
   */
  public static URL mainClassURL() {
    /* Id�e : on cr�e une exception et on va travailler dans la trace
     * pour trouver le nom de la classe de la m�thode main.
     * Ensuite on recherche cette classe comme une ressource.
     * Le r�sultat de getResource donne un URL. On regarde si cet URL
     * correspond � un jar ou non.
     */
    Throwable t = new Throwable();
    StackTraceElement[] elements = t.getStackTrace();
    String classePrincipale = elements[elements.length - 1].getClassName();
    // Recherche de la classe comme ressource
    // On transforme d'abord le nom de la classe en chemin de ressource
    // � partir du classpath :
    String nomRessource = classePrincipale.replace('.', '/');
    nomRessource = "/" + nomRessource + ".class";
    //    System.out.println("nom de la classe comme ressource = " + nomRessource);
    URL urlClassePrincipale = null;
    try {
      urlClassePrincipale = Class.forName(classePrincipale).getResource(nomRessource);
      //      System.out.println("urlClassePrincipale = " + urlClassePrincipale);
    } catch(ClassNotFoundException e) {
    }
    return urlClassePrincipale;

  }

  /**
   * D�tecte si la classe principale de l'application est dans un jar ou non.
   *
   * @return l'URL de la classe principale de l'application si elle est dans un jar. Retourne
   * <code>null</code> si l'application n'est pas dans un jar.
   */
  public static boolean mainInJar() {
    /* Id�e : on cr�e une exception et on va travailler dans la trace
     * pour trouver le nom de la classe de la m�thode main.
     * Ensuite on recherche cette classe comme une ressource.
     * Le r�sultat de getResource donne un URL. On regarde si cet URL
     * correspond � un jar ou non.
     */
    return mainClassURL().getProtocol().equals("jar");

  }
}