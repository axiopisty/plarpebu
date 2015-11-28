package com.plarpebu.plugins.sdk;

/**
 * <p>
 * Title: SystemExitListener
 * </p>
 * <p>
 * Description: Le SystemExitListener sert � signaler un appel � System.exit(). Juste avant de
 * sortir de la JVM la fonction exiting est appel�e.
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
   * Fonction appel�e avant la fin du programme.
   */
  public void exiting();
}
