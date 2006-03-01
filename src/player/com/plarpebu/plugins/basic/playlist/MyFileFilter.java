package com.plarpebu.plugins.basic.playlist;

import java.io.File;
import java.io.FileFilter;

public class MyFileFilter implements FileFilter {

    private String extension = "";

    public boolean accept(File pathName) {
        if (pathName.isDirectory()) return true;
        String ext = pathName.getName();
        ext = ext.substring(ext.lastIndexOf(".") + 1, ext.length());
        if (ext.equals(extension)) { // check for any extension you want to
                                        // list
            return true;
        }
        return false;
    }

    public void setExtension(String ex) {
        extension = ex;
    }

    public String getExtension() {
        return extension;
    }
}
