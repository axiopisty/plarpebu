package pluginsSDK;

import plugins.examples.AudioInfoPlugin;
import plugins.examples.EqualizerPlugin;
import plugins.playlist.PlayListPlugin;

/**
 * La classe FramePlugin est la classe mere de tous les plugins qui se rajoutent
 * en creeant une nouvelle fenetre. Elle etend JFrame et doit implementer bien
 * evidemment playerPlugin. Cette classe est logiquement abstraite et nous
 * servira pour le polymorphisme.
 * 
 * @author ARNAUD & PEPINO
 * @version finale
 * @see EqualizerPlugin
 * @see AudioInfoPlugin
 * @see PlayListPlugin
 */
abstract public class FramePlugin extends JFrameWithPreferences implements PlayerPlugin {

    private String preferencesDir = "preferences";

    private String preferenceFileName = getName() + ".properties";

    private String defaultPreferencesFilename = "default" + getName() + ".properties";

    public FramePlugin() {
        // Specify the dir and filenames for preferences files
        setPreferencesFileNames(preferencesDir, preferenceFileName, defaultPreferencesFilename);
    }

    /**
     * Cette fonction retourne le type du plugin.
     * 
     * @return la classe du plugin.
     */
    public Class getType() {
        return FramePlugin.class;
    }

    /**
     * Cette fonction permet d'obtenir une description du plugin
     * 
     * @return la description du plugin
     */
    public String getDescription() {
        return "no Description";
    }

    public boolean canProcess(Object o) {
        return true;
    }

    public boolean matches(Class type, String name, Object object) {
        return true;
    }

    public String getVersion() {
        return "v1.0";
    }

}
