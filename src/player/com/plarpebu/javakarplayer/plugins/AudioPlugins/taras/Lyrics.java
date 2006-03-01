package com.plarpebu.javakarplayer.plugins.AudioPlugins.taras;

/*
 * Low level processing of the MIDI sequence
 *
 * 3 July 2002
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.mozilla.intl.chardet.HtmlCharsetDetector;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * Main lyrics formatter
 */
public class Lyrics {
    public static final int SET_TITLE = 1;

    public static final int SET_ARTIST = 2;

    public static final int DO_NOTHING = 3;

    private static int state = SET_TITLE;

    private static String title = null;

    private static String artist = null;

    private static boolean charsetAutodetection;

    private static String currentCharset = "ISO8859_1";

    private static int charsetHint = nsPSMDetector.ALL;

    public static String getArtist() {
        return artist;
    }

    public static String getTitle() {
        return title;
    }

    private static byte[] smooth(byte[] data) {
        int i = 0;
        for (; i < data.length && data[i] != 0; i++);
        if (i == data.length) return data;

        byte[] data1 = new byte[i];
        for (int j = 0; j < i; ++j) {
            if (Character.isISOControl((char) data[j])) {
                data1[j] = 0x20;
            }
            else {
                data1[j] = data[j];
            }

            // data1[j] = data[j] < 0x20 ? data1[j] = 0x20 : data[j];
        }

        return data1;
    }

    /**
     * Reads lyrics from MIDI sequence assuming text to be in given charset.
     * Creates vector of syllables
     * 
     * @param seq
     *        sequence to process
     * @param charset
     *        text charset
     * @return vector of syllables
     */
    public static Vector read(Sequence seq) throws IOException, UnsupportedEncodingException {

        long startTime = System.currentTimeMillis();

        if (charsetAutodetection) {
            System.out.println("====CHARSET EN AUTODETECTION=====");
            currentCharset = findCharset(seq);
        }

        Track[] tracks = seq.getTracks();
        Vector song = new Vector();

        for (int i = 0; i < tracks.length; ++i) {
            for (int j = 0; j < tracks[i].size(); ++j) {
                MidiEvent me = tracks[i].get(j);
                MidiMessage mm = me.getMessage();
                if (mm.getStatus() == 0xFF) {
                    MetaMessage meta = (MetaMessage) me.getMessage();
                    int type = meta.getType();

                    if (type == Syllable.ST_TEXT || type == Syllable.ST_LYRICS || type == Syllable.ST_COPYRIGHT) {

                        Syllable syl = new Syllable();
                        syl.me = me;
                        syl.track = i;
                        syl.type = type;
                        syl.text = new String(smooth(meta.getData()), currentCharset);
                        song.add(syl);
                    }
                }
            }

        }
        System.out.println("Sequence read in " + (System.currentTimeMillis() - startTime));
        return song;
    }

    private static String findCharset(Sequence seq) {
        long startTime = System.currentTimeMillis();

        String charset = "";
        Track[] tracks = seq.getTracks();

        boolean done = false;
        boolean found = false;
        boolean isAscii = true;

        nsDetector det = new nsDetector(charsetHint);
        System.out.println("Starting autodetection hint = " + charsetHint);
        // Set an observer...
        // The Notify() will be called when a matching charset is found.
        det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
                HtmlCharsetDetector.found = true;
                System.out.println("CHARSET =TROUVE ==== " + charset);
            }
        });

        // DANGER !!! Should try all tracks ?
        int nbTracks = tracks.length;
        if (nbTracks >= 4) nbTracks = 4;

        for (int i = 0; i < nbTracks; ++i) {
            for (int j = 0; j < tracks[i].size(); ++j) {
                MidiEvent me = tracks[i].get(j);
                MidiMessage mm = me.getMessage();
                if (mm.getStatus() == 0xFF) {
                    MetaMessage meta = (MetaMessage) me.getMessage();
                    int type = meta.getType();

                    if (type == Syllable.ST_TEXT || type == Syllable.ST_LYRICS || type == Syllable.ST_COPYRIGHT) {

                        // Do not smooth diring charset detection
                        // byte[] buf = smooth(meta.getData());
                        byte[] buf = meta.getData();

                        // Check if the stream is only ascii.
                        if (isAscii) {
                            isAscii = det.isAscii(buf, buf.length);

                        }

                        // DoIt if non-ascii and not done yet.
                        if (!isAscii && !done) {
                            done = det.DoIt(buf, buf.length, false);
                        }

                    }
                }
            }

            System.out.println("Find charset done in " + (System.currentTimeMillis() - startTime));
        }

        det.DataEnd();

        if (isAscii) {
            System.out.println("CHARSET = ASCII");
            found = true;
            charset = "ASCII";
        }

        if (!found) {
            String prob[] = det.getProbableCharsets();
            for (int i = 0; i < prob.length; i++) {
                System.out.println("Probable Charset = " + prob[i]);
            }
            // Most probable charset, first one is first guess etc... so the
            // last one
            // is the best choice
            charset = prob[prob.length - 1];
        }
        return charset;
    }

    /**
     * Finds the longest track of events of the given type
     * 
     * @param song
     *        vector of syllables
     * @param type
     *        type of events to take into account
     * @return index of the track or -1 on error
     */
    private static int longest_track(Vector song, int type) {
        /* find lyrics track - the INFO with the most number of text */
        int track = -1, num = 0; // current track
        int ltrack = -1, ltrack_num = 0; // best track

        for (int i = 0; i < song.size(); ++i) {
            Syllable syl = (Syllable) song.elementAt(i);
            if (syl.track != track) { // track change
                if (num > ltrack_num) {
                    ltrack = track;
                    ltrack_num = num;
                }
                num = 0;
                track = syl.track;
            }

            if (syl.type == type && syl.text.length() > 0 && syl.text.charAt(0) != '@') ++num;
        }

        /* check the last track */
        if (num > ltrack_num) ltrack = track;

        return ltrack;
    }

    /*
     * Preformats the lyrics: each syllable gets its line number, long
     * lines/words are broken and wrapped to fit into the given limit I
     * KarMaker's files have the following arrangements: 1. all lyrics is placed
     * in the single track 2 . song/artist/file composer name is placed as text
     * meta-event with "@T" prefix 3. language info is stored with "@L" prefix
     * as text event 4. all lyrics is text events. 5. new line is started with
     * "/" 6. new paragraph is started with "\" II Karaoke.ru files - ? - new
     * line may also start with "//" @param song text to format @param cols
     * number of columns to use for wrapping
     */
    public static void preformat(Vector song, int cols) {
        state = SET_TITLE;
        int type = Syllable.ST_TEXT;
        int ltrack = longest_track(song, type);

        if (ltrack == -1) {
            type = Syllable.ST_LYRICS;
            ltrack = longest_track(song, type);
            if (ltrack == -1) return; // no lyrics
        }

        /*
         * sift all text and leave only that from the ltrack. set line numbers
         */
        int line = 0, length = 0;

        for (int i = 0; i < song.size(); ++i) {
            Syllable syl = (Syllable) song.elementAt(i);
            syl.line = -1;

            /* comment non-lyrics events */
            if (syl.track != ltrack || syl.text.length() == 0) {
                syl.type = Syllable.ST_COMMENT;
                continue;
            }

            /*
             * we can't just skip all events in this track but should check them
             * so that they should not mix with valid lyrics
             */
            if (syl.type != type) {
                if ((syl.type != Syllable.ST_TEXT && syl.type != Syllable.ST_LYRICS) || syl.text.charAt(0) != '@')
                    continue;
            }
            syl.type = Syllable.ST_TEXT;

            int save_line = line;
            if (syl.text.charAt(0) == '@') { /* comment */

                // Just get song and artisi names
                if (syl.text.charAt(1) == 'T') {
                    switch (state) {
                    case SET_TITLE:
                        title = syl.text.substring(2);
                        state = SET_ARTIST;
                        break;
                    case SET_ARTIST:
                        artist = syl.text.substring(2);
                        state = DO_NOTHING;
                        break;
                    }

                }

                syl.type = Syllable.ST_COPYRIGHT;
                syl.text = syl.text.substring(2);
            }
            else if (syl.text.charAt(0) == '\\') { /* new paragraph */
                line += 2;
                syl.line = line;
                syl.text = syl.text.substring(1);
            }
            else if (syl.text.charAt(0) == '/' && syl.text.length() > 1 && syl.text.charAt(1) == '/') {
                syl.line = ++line;
                syl.text = syl.text.substring(2);
            }
            else if (syl.text.charAt(0) == '/') { /* new line */
                syl.line = ++line;
                syl.text = syl.text.substring(1);
            }
            else {
                syl.line = line;
            }

            if (syl.type == Syllable.ST_TEXT) {
                if (syl.line != save_line) length = 0;
                length += syl.text.length();
                if (length > cols) { // need to break line
                    if (syl.text.length() > cols) {
                        // too long word or too small window
                        Syllable s = new Syllable();
                        s.line = 0;
                        s.type = type;
                        s.track = syl.track;
                        s.me = syl.me;
                        int y = syl.text.length() - length + cols;
                        ;
                        s.text = syl.text.substring(y);
                        syl.text = syl.text.substring(0, y);
                        /* this syllable will be passed anew */
                        song.add(i + 1, s);
                        ++line;
                        length = 0;
                    }
                    else {
                        syl.line = ++line;
                        length = syl.text.length();
                    }
                }
            }
        }
    }

    /**
     * Formats frame in time segment [frame.start, frame.end)
     * 
     * @param frame
     *        frame to format
     * @param song
     *        lyrics
     * @param read_line
     *        reading line
     * @param total_lines
     *        overall number of lines in the frame
     */
    static void format_frame(KFrame frame, Vector song, int read_line, int total_lines) {
        int ref_point, i, clear_start;
        for (ref_point = 0; ref_point < song.size(); ++ref_point) {
            Syllable syl = (Syllable) song.elementAt(ref_point);
            if (syl.type != Syllable.ST_TEXT) continue;
            if (syl.me.getTick() >= frame.end) break;
        }

        clear_start = ref_point;
        // count one syllable back 'cause this one is not yet highlighted
        for (--ref_point; ref_point >= 0 && ((Syllable) song.elementAt(ref_point)).type != Syllable.ST_TEXT; --ref_point);

        // ref_point points to the last syllable highlighted in this frame
        // or -1 if such doesn't exist
        // count back the visible syllables in this frame from the
        // reference point
        for (i = ref_point; i >= 0; --i) {
            Syllable syl = (Syllable) song.elementAt(i);
            if (syl.type != Syllable.ST_TEXT) continue;
            if ((((Syllable) song.elementAt(ref_point)).line - syl.line) > read_line) break;
        }

        ++i; // get back, i >= 0
        // i points to the first syllable in visible the frame

        if (ref_point >= 0) {
            Syllable syl = (Syllable) song.elementAt(ref_point);
            frame.first_line = syl.line >= read_line ? syl.line - read_line : 0;
        }
        else {
            Syllable syl = (Syllable) song.elementAt(clear_start);
            frame.first_line = clear_start < song.size() ? (syl.line >= read_line ? syl.line - read_line : 0) : 0;
        }

        // find first visible syllable line
        int first_visible_line = 0;
        for (int k = 0; k < song.size(); ++k) {
            Syllable syl = (Syllable) song.elementAt(k);
            if (syl.type == Syllable.ST_TEXT) {
                first_visible_line = syl.line;
                break;
            }
        }

        if (first_visible_line > frame.first_line) {
            // eliminate empty leading lines
            frame.first_line = first_visible_line;
        }

        for (; i <= ref_point; ++i) {
            Syllable syl = (Syllable) song.elementAt(i);
            if (syl.type != Syllable.ST_TEXT) continue;
            frame.sung.add(syl);
        }

        // count forward the syllables seen in this frame from the
        // reference point
        for (i = clear_start; i < song.size()
        && ((((Syllable) song.elementAt(i)).line - frame.first_line) < total_lines); ++i) {
            Syllable syl = (Syllable) song.elementAt(i);
            if (syl.type != Syllable.ST_TEXT) continue;
            frame.clear.add(syl);
        }
    }

    /**
     * Generates series of variable length frames with given parameters.
     * 
     * @param song
     *        lyrics to format
     * @param read_line
     *        reading line
     * @param total_lines
     *        overall number of lines in the frame
     * @param song_duration
     *        song length in ticks
     * @return vector of frames
     */
    public static Vector format(Vector song, int read_line, int total_lines, long song_duration) {
        long start = 0, i = 0;
        int line = -1; // redraw first frame
        Vector base = new Vector();

        int j = 0, end = song.size();
        do {
            Syllable syl = j < end ? (Syllable) song.elementAt(j) : null;
            if (j == end || syl.type == Syllable.ST_TEXT) {
                KFrame frame = new KFrame();
                frame.start = start;
                frame.end = j == end ? song_duration : syl.me.getTick();
                frame.to_prev_key_frame = 0;
                frame.to_next_key_frame = frame.end - frame.start;
                base.add(frame);
                format_frame(frame, song, read_line, total_lines);
                start = frame.end;
                frame.scrolled = frame.first_line != line;
                line = frame.first_line;
                ++i;
            }
        }
        while (j++ != end);

        return base;
    }

    /**
     * Creates array of text strings
     * 
     * @param song
     *        lyrics as syllables
     * @return lyrics as text strings
     */
    static public Vector makeText(Vector song) {
        String str = "";
        int line = -1;
        Vector lines = new Vector();

        for (int i = 0; i < song.size(); ++i) {
            Syllable syl = (Syllable) song.elementAt(i);
            if (syl.type != Syllable.ST_TEXT) continue;
            if (syl.line != line) { /* new line */
                if (str.length() > 0) lines.add(str);
                str = syl.text;
                line = syl.line;
            }
            else {
                str += syl.text;
            }
        }

        if (str.length() > 0) lines.add(str);
        return lines;
    }

    public static boolean isCharsetAutodetection() {
        return charsetAutodetection;
    }

    public static void setCharsetAutodetection(boolean flag) {
        charsetAutodetection = flag;
    }

    public static String getCurrentCharset() {
        return currentCharset;
    }

    public static void setCurrentCharset(String charset) {
        currentCharset = charset;
    }

    public static void setCharsetHint(int value) {
        charsetHint = value;
    }

}