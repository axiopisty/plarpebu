package basicplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import util.CacheUtil;

/**
 * Zip utilities
 *
 * @author kmschmidt
 */
public class ZipUtil {
    /**
     * Unzip a "Zipped MP3G" file to a temp location and return the MP3 File
     * object
     */
    public static File unzipMP3G(File file) {
        try {
            ZipEntry mp3OrMidiKarEntry = null;
            ZipEntry cdgEntry = null;
            String filename="";

            ZipFile zipFile = new ZipFile(file);
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                 filename = entry.getName().toLowerCase();
                if (filename.endsWith(".mp3") || filename.endsWith(".mid") || filename.endsWith(".kar"))
                    mp3OrMidiKarEntry = entry;
                else if (entry.getName().toLowerCase().endsWith(".cdg")) cdgEntry = entry;
            }

            if (filename.endsWith(".mp3") && (cdgEntry == null)) {
                JOptionPane.showMessageDialog(null, "Zip file does not contain a CDG file.", "Warning",
                JOptionPane.WARNING_MESSAGE);
            }

            if (mp3OrMidiKarEntry == null) {
                JOptionPane.showMessageDialog(null, "Zip file does not contain an MP3, mid or kar file.", "Warning",
                JOptionPane.WARNING_MESSAGE);
                return null;
            }

            //Get mp3g temp cache dir
            File tempDir = CacheUtil.getMP3GCacheDir();

            // Delete any preexisting mp3g temp files
            File[] files = tempDir.listFiles();
            for (int i = 0; i < files.length; ++i)
                files[i].delete();

            // Write the zipped mp3, kar or mid file out to a file if necessary
            File mp3MidKarFile = new File(tempDir, mp3OrMidiKarEntry.getName());
            if (mp3MidKarFile.exists() == false) {
                FileOutputStream fos = new FileOutputStream(mp3MidKarFile);
                InputStream is = zipFile.getInputStream(mp3OrMidiKarEntry);

                System.out.println("Writing temp file: " + mp3MidKarFile.getName());
                byte[] buffer = new byte[1024];
                int size;
                while ((size = is.read(buffer, 0, 1024)) != -1)
                    fos.write(buffer, 0, size);

                fos.close();
                is.close();
            }

            // Write the zipped cdg out to a file
            if(filename.endsWith(".mp3")) {
              File cdgFile = new File(tempDir, cdgEntry.getName());
              if (cdgFile.exists() == false) {
                FileOutputStream fos = new FileOutputStream(cdgFile);
                InputStream is = zipFile.getInputStream(cdgEntry);

                System.out.println("Writing temp file: " + cdgFile.getName());
                byte[] buffer = new byte[1024];
                int size;
                while ( (size = is.read(buffer, 0, 1024)) != -1)
                  fos.write(buffer, 0, size);

                fos.close();
                is.close();
              }
            }

            return mp3MidKarFile;
        }
        catch (Exception zex) {
            JOptionPane.showMessageDialog(null, "Error reading zip file: " + zex, "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}


