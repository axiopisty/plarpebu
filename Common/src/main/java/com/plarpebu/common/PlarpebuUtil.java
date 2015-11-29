package com.plarpebu.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Optional;

public final class PlarpebuUtil {

  private final static Logger logger = LoggerFactory.getLogger(PlarpebuUtil.class);

  private static Optional<File> opApplicationRoot = Optional.empty();

  public static File applicationRootDirectory() {
    return opApplicationRoot.orElse(memoizeApplicationRoot());
  }

  private static File memoizeApplicationRoot() {
    File root = null;
    try {
      CodeSource codeSource = PlarpebuUtil.class.getProtectionDomain().getCodeSource();
      root = new File(URLDecoder.decode(new File(
        codeSource
          .getLocation()
          .toURI()
          .getPath()
      ).getParentFile()
       .getParentFile()
       .getPath(), "UTF-8"));
      opApplicationRoot = Optional.of(root);
    } catch(Throwable t) {
      logger.warn(t.getMessage(), t);
    }
    return root;
  }

}
