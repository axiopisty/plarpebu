package com.plarpebu.javakarplayer.plugins.AudioPlugins.taras;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

/**
 * Represents syllables of text
 */
class Syllable
{
	public static final int ST_COMMENT = 0;

	public static final int ST_LYRICS = 5;

	public static final int ST_TEXT = 1;

	public static final int ST_COPYRIGHT = 2;

	public int line;

	public int track;

	public String text;

	public int type;

	public MidiEvent me;

	int getMsgType()
	{
		return ((MetaMessage) me.getMessage()).getType();
	}
}
