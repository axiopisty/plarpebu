package javakarplayer.plugins.AudioPlugins.taras;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

/**
 * Set of properties used by the player
 */
public class KaraokeProperties {
    public static final int BACKGROUND_PLAIN = 0;

    public static final int BACKGROUND_GRADIANT = 1;

    public static final int BACKGROUND_IMAGE = 2;

    public int backgroundType = BACKGROUND_GRADIANT;

    public String fontFace, charset;

    public int style;

    public int width, height;

    public Color bgColor;

    public Color syllabesNotSungYetColor;

    public Color syllabeToSingColor;

    public Color ribbonColor;

    public Color startGradiantColor, endGradiantColor;

    public Color shadowColor;

    public Color outlineColor;

    public Color sungSyllabesColor;

    public int lines, cols, readLine, ribbonWidth;

    public boolean displayShadow;

    public boolean displayOutline;

    public int outlineWidth = 3;

    public boolean autodetectCharset;

    public int charsetHint = 0;

    public String bgImageFilename = "noImage";

    public boolean antiAliasedText;

    Image bgImage;

    /**
     * Default karaoke properties
     */
    public final int backgroundMode = 0;

    public KaraokeProperties() {
        fontFace = "Comic sans MS";
        style = Font.PLAIN;
        charset = "ISO8859_1"; // "windows-1251";
        width = 800;
        height = 600;
        bgColor = Color.blue;
        syllabesNotSungYetColor = Color.yellow;
        sungSyllabesColor = Color.red;
        syllabeToSingColor = Color.green;
        ribbonColor = Color.red;
        startGradiantColor = new Color(0, 0, 255);
        endGradiantColor = new Color(0, 0, 50);
        shadowColor = Color.black;
        outlineColor = Color.black;
        lines = 6; // 6
        cols = 50; // 35
        readLine = 1;
        ribbonWidth = 4;
        bgImage = null;
        displayShadow = true;
        displayOutline = true;
        autodetectCharset = false;
        antiAliasedText = false;
    }
}