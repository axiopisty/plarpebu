package com.plarpebu.javakarplayer.plugins.AudioPlugins.taras;

import java.util.Vector;

/**
 * Represents formatted frame
 */
class KFrame
{
	public long start, end, to_next_key_frame, to_prev_key_frame;

	public int first_line;

	public Vector sung;

	public Vector clear;

	boolean scrolled;

	public KFrame()
	{
		sung = new Vector();
		clear = new Vector();
	}

	public String toString()
	{
		String str = "[" + start + ", " + end + "), first line = " + first_line + "\n";

		for (int i = 0; i < sung.size(); ++i)
		{
			Syllable syl = (Syllable) sung.elementAt(i);
			str += syl.text;
		}

		str += "\n\n";

		for (int i = 0; i < clear.size(); ++i)
		{
			Syllable syl = (Syllable) clear.elementAt(i);
			str += syl.text;
		}

		return str;
	}
}
