package com.plarpebu.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

/**
 * Zip utilities
 *
 * @author kmschmidt
 */
public class ZipUtil {

  private final static Logger logger = LoggerFactory.getLogger(ZipUtil.class);

  /**
   * Unzip a "Zipped MP3G" file to a temp location and return the MP3 File object
   */
  public static File unzipMP3G(File file) {
    try {
      ZipEntry mp3Entry = null;
      ZipEntry cdgEntry = null;

      ZipFile zipFile = new ZipFile(file);
      Enumeration entries = zipFile.entries();
      while(entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) entries.nextElement();
        if(entry.getName().toLowerCase().endsWith(".mp3")) {
          mp3Entry = entry;
        } else if(entry.getName().toLowerCase().endsWith(".cdg")) {
          cdgEntry = entry;
        }
      }

      if(cdgEntry == null) {
        JOptionPane
          .showMessageDialog(null, "Zip file does not contain a CDG file.", "Warning", JOptionPane.WARNING_MESSAGE);
      }

      if(mp3Entry == null) {
        JOptionPane
          .showMessageDialog(null, "Zip file does not contain an MP3 file.", "Warning", JOptionPane.WARNING_MESSAGE);
        return null;
      }

      // Get mp3g temp cache dir
      File tempDir = CacheUtil.getMP3GCacheDir();

      // Delete any preexisting mp3g temp files
      File[] files = tempDir.listFiles();
      for(int i = 0; i < files.length; ++i) {
        files[i].delete();
      }

      // Write the zipped mp3 out to a file if necessary
      File mp3File = new File(tempDir, mp3Entry.getName());
      if(mp3File.exists() == false) {
        FileOutputStream fos = new FileOutputStream(mp3File);
        InputStream is = zipFile.getInputStream(mp3Entry);

        logger.debug("Writing temp file: " + mp3File.getName());
        byte[] buffer = new byte[1024];
        int size;
        while((size = is.read(buffer, 0, 1024)) != -1) {
          fos.write(buffer, 0, size);
        }

        fos.close();
        is.close();
      }

      // Write the zipped cdg out to a file
      File cdgFile = new File(tempDir, cdgEntry.getName());
      if(cdgFile.exists() == false) {
        FileOutputStream fos = new FileOutputStream(cdgFile);
        InputStream is = zipFile.getInputStream(cdgEntry);

        logger.debug("Writing temp file: " + cdgFile.getName());
        byte[] buffer = new byte[1024];
        int size;
        while((size = is.read(buffer, 0, 1024)) != -1) {
          fos.write(buffer, 0, size);
        }

        fos.close();
        is.close();
      }

      return mp3File;
    } catch(Exception zex) {
      JOptionPane.showMessageDialog(null, "Error reading zip file: " + zex, "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }
}
