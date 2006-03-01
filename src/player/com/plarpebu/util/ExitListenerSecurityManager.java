package com.plarpebu.util;

import java.io.FileDescriptor;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import com.plarpebu.pluginsSDK.SystemExitListener;

/**
 * <p>
 * Title: ExitListener Security Manager
 * </p>
 * <p>
 * Description: Cette classe sert &agrave; interdire momentan&eacute;ment
 * l'appelle &agrave; la fonction exit. Elle envoie une
 * {@link SecurityException} si on appelle la fonction exit une fois qu'on l'a
 * verrouill&eacute; avec la fonction {@link #denyExit}. On peut
 * d&eacute;verrouiller avec la fonction {@link #permitExit}. <br>
 * Les permissions accord&eacute;es par ce {@link SecurityManager} sont les
 * suivantes: <br>
 * Dans les {@link RuntimePermission}: <tt>createClassLoader</tt>,
 * <tt>setIO</tt> et <tt>accessClassInPackage.sun.reflect</tt>.<br>
 * Dans les {@link SecurityPermission}:
 * <tt>getProperty.networkaddress.cache.ttl</tt> et
 * <tt>getProperty.networkaddress.cache.negative.ttl</tt>.<br>
 * Dans les {@link PropertyPermission}: <tt>sun.net.inetaddr.ttl</tt> et
 * <tt>file.encoding</tt>.<br>
 * Dans les {@link ReflectPermission}: <tt>suppressAccessChecks</tt>.<br>
 * Toutes les {@link FilePermission} et {@link LoggingPermission} sont permises.
 * </p>
 * Au moment de la sortie du programme, le SecurityManager le signal ˆ tous les
 * SystemExitListener.<br>
 * <p>
 * Copyright: Copyright (c) 2003-2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Julien Charles
 * @version 1.0
 */
public class ExitListenerSecurityManager extends SecurityManager {
    private boolean bCanExit;

    private ArrayList exitListeners;

    /**
     * Constructeur: l'acc&egrave;s est permis a la fonction exit par
     * d&eacute;faut.
     */
    public ExitListenerSecurityManager() {
        super();
        bCanExit = true;
        exitListeners = new ArrayList();
    }

    /**
     * Ajoute un SystemExitListener au sŽcurity manager.
     * 
     * @param listener
     *        le ExitListener ˆ ajouter.
     */
    public void addSystemExitListener(SystemExitListener listener) {
        exitListeners.add(listener);
    }

    /**
     * garantit un certain nombre de permissions de base.
     */
    public void checkPermission(Permission perm) {
        /*
         * if // Les permissions du Runtime
         * (perm.getName().equals("createClassLoader") ||
         * perm.getName().equals("setIO") || perm.getName().equals(
         * "accessClassInPackage.sun.reflect") || // Les permissions de
         * sécurité... perm.getName().equals(
         * "getProperty.networkaddress.cache.ttl") || perm.getName().equals(
         * "getProperty.networkaddress.cache.negative.ttl" ) || // Les
         * permissions de Property perm.getName().equals("sun.net.inetaddr.ttl") ||
         * perm.getName().equals("file.encoding")) { return; } else if ((perm
         * instanceof RuntimePermission) || (perm instanceof
         * PropertyPermission)|| (perm instanceof SecurityPermission)|| (perm
         * instanceof FilePermission) || (perm instanceof LoggingPermission)) {
         * return; } else if ((perm instanceof ReflectPermission) &&
         * perm.getName().equals("suppressAccessChecks")) { } else {
         * super.checkPermission(perm); }
         */
        return;
    }

    /**
     * permet cet acc&egrave;s quel que soit l'argument.
     */
    public void checkDelete(String file) {
        return;
    }

    /**
     * permet cet acc&egrave;s quel que soit l'argument.
     */
    public void checkRead(String file) {
        return;
    }

    /**
     * permet cet acc&egrave;s quel que soit l'argument.
     */
    public void checkRead(FileDescriptor fd) {
        return;
    }

    /**
     * permet cet acc&egrave;s quel que soit l'argument.
     */
    public void checkWrite(String file) {
        return;
    }

    /**
     * permet cet acc&egrave;s quel que soit l'argument.
     */
    public void checkWrite(FileDescriptor fd) {
        return;
    }

    /**
     * Si la fonction {@link #denyExit} a &eacute;t&eacute; appel&eacute;e avant
     * cette fonction, cette derni&egrave;re renvoit une
     * {@link SecurityException}.
     */
    public void checkExit(int status) {
        Iterator iter = exitListeners.listIterator();
        while (iter.hasNext()) {
            ((SystemExitListener) iter.next()).exiting();
        }
        if (!bCanExit) throw new SecurityException((new Integer(status)).toString());
    }

    /**
     * Interdit de sortir de la JVM par {@link System#exit(int)}.
     */
    public void denyExit() {
        bCanExit = false;
    }

    /**
     * Permet de sortir de la JVM par {@link System#exit(int)}.
     */
    public void permitExit() {
        bCanExit = true;
    }
}
