package com.plarpebu.common;

import java.io.File;

import javazoom.jlgui.basicplayer.BasicController;

public interface PlayerController extends BasicController {

  boolean isFileSupported(File file);

  boolean isFileSupported(String file);

  String[] getSupportedFileTypeExtensions();
}
