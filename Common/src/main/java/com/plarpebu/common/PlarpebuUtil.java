package com.plarpebu.common;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class PlarpebuUtil {

  private static Optional<File> opApplicationRoot = Optional.empty();

  public static File applicationRootDirectory() {
    return opApplicationRoot.orElse(memoizeApplicationRoot());
  }

  private static File memoizeApplicationRoot() {
    File root = null;
    try {
      CodeSource codeSource = PlarpebuUtil.class.getProtectionDomain().getCodeSource();
      root = new File(
        URLDecoder.decode(
          new File(codeSource.getLocation().toURI().getPath())
            .getParentFile()
            .getParentFile()
            .getPath()
          ,"UTF-8"
        )
      );
      opApplicationRoot = Optional.of(root);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return root;
  }

  private static FileHandler fileHandler;

  static {
    try {
      // This block configure the logger with handler and formatter
      File appRoot = applicationRootDirectory();
      String logFile = new File(appRoot, "plarpebu.log").getCanonicalPath();
      fileHandler = new FileHandler(logFile);
      SimpleFormatter formatter = new SimpleFormatter();
      fileHandler.setFormatter(formatter);
    } catch (SecurityException | IOException e) {
      e.printStackTrace();
    }
  }

  public static Logger configureLogToFile(Logger logger) {
    logger.addHandler(fileHandler);
    return logger;
  }
}
