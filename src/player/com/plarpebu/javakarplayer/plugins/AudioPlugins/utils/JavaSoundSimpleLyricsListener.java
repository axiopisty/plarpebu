package com.plarpebu.javakarplayer.plugins.AudioPlugins.utils;

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

    public void clearScreen() {
        System.out.println("\n\n");
    };

    public void newLine() {
        System.out.println("\n");
    };

    public void outputLyric(String lyric) {
        System.out.print(lyric);
    };

};
