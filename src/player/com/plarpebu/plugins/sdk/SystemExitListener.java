package com.plarpebu.plugins.sdk;

/**
 * <p>
 * Title: SystemExitListener
 * </p>
 * <p>
 * Description: Le SystemExitListener sert à signaler un appel à System.exit().
 * Juste avant de sortir de la JVM la fonction exiting est appelée.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Julien Charles
 * @version 1.0
 */

public interface SystemExitListener {
    /**
     * Fonction appelée avant la fin du programme.
     */
    public void exiting();
}
