package com.plarpebu.javakarplayer.plugins.AudioPlugins.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author Michel Buffa (buffa@unice.fr)
 * @version $Id
 */
public class JavaSoundSimpleLyricsListener extends JavaSoundLyricsListener {

  private final static Logger logger = LoggerFactory.getLogger(JavaSoundSimpleLyricsListener.class);

  public void clearScreen() {
    logger.debug("\n\n");
  }

  ;

  public void newLine() {
    logger.debug("\n");
  }

  ;

  public void outputLyric(String lyric) {
    logger.debug(lyric);
  }

  ;

};
