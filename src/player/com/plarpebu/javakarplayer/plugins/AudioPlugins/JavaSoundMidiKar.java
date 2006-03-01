package com.plarpebu.javakarplayer.plugins.AudioPlugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import com.plarpebu.javakarplayer.plugins.AudioPlugins.taras.Karaoke;
import com.plarpebu.plugins.sdk.Iconifiable;

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
public class JavaSoundMidiKar implements MetaEventListener {

    private Sequencer sequencer;

    private Karaoke visualKaraoke;

    private Synthesizer synthesizer;

    private MidiChannel channels[];

    public static final int FINISH_EVENT = 47;

    private long songLength;

    // TO BE CORRECTED
    private MidiListener player;

    private String artist;

    private String songTitle;

    private boolean playingKar = false;

    // Current volume and pan...
    private double currentGain = 1.0;

    private double currentPan = 1.0;

    public JavaSoundMidiKar() {

        // Init the midi
        try {
            sequencer = MidiSystem.getSequencer();
            if (sequencer instanceof Synthesizer)
                synthesizer = (Synthesizer) sequencer;
            else
                synthesizer = MidiSystem.getSynthesizer();

            channels = synthesizer.getChannels();

            // // Link up the Synthesizer to the Sequencer ...
            if (!(sequencer instanceof Synthesizer)) {
                Transmitter seqTrans = sequencer.getTransmitter();
                Receiver synthRecv = synthesizer.getReceiver();
                seqTrans.setReceiver(synthRecv);
            }

            visualKaraoke = new Karaoke(sequencer);

            // / Juste a simple lyrics listener
            // lyricsListener = new JavaSoundSimpleLyricsListener();
            // sequencer.addMetaEventListener(lyricsListener);

            // For detecting end of song
            sequencer.addMetaEventListener(this);
        }
        catch (MidiUnavailableException ex) {
            System.out.println("Your system does not support midi sound !");
        }
    }

    public void setPlayerUI(Iconifiable playerUI) {
        visualKaraoke.setPlayerUI(playerUI);
    }

    /**
     * This method is necessary to detect the end of a midi song
     * 
     * @param meta
     */
    public void meta(MetaMessage meta) {
        if (meta.getType() == FINISH_EVENT) {
            stop();
        }
    }

    public void setMidiListener(MidiListener listener) {
        player = listener;
    }

    public int loadFile(File file) {
        if ((file != null)
        && (((file.toString()).toLowerCase().endsWith(".kar")) || ((file.toString()).toLowerCase().endsWith(".mid")))) {
            try {
                sequencer.open();
                Sequence seq = MidiSystem.getSequence(file);
                sequencer.setSequence(seq);
                songLength = sequencer.getSequence().getTickLength();

                try {
                    playingKar = visualKaraoke.setSong(seq);
                    artist = visualKaraoke.getArtist();
                    songTitle = visualKaraoke.getSongTitle();
                }
                catch (Exception ex1) {
                    System.out.println("Problem with processing the kar file : " + file.toString());
                }

                if (!playingKar) {
                    // We do not have a karaoke file, hide the kar lyrics
                    // display
                    System.out.println("loadfile, on cache visual karaoke");
                    visualKaraoke.setVisible(false);
                }
                return 1;
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                return 0;
            }
            catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
            catch (MidiUnavailableException ex) {
                System.out.println("Midi not available on this machine");
            }
            catch (InvalidMidiDataException ex) {
                System.out.println("InvalidMidiDataException !!!!");
            }

        }
        else
            System.out.println("File format not supported");
        return 0;
    }

    public String getName() {
        return "Java Sound midi plugin";
    }

    public void play() {
        if (sequencer != null) {

            // If we are playing a kar file, be sure that the JFrame for lyrics
            // is showing
            if (playingKar) {
                visualKaraoke.setVisible(true);
                while (!visualKaraoke.isShowing());
                new Ticker();
            }

            sequencer.start();
            // set volume and pan to current values
            setGain(currentGain);
            setPan(currentPan);
        }
    }

    public void stop() {
        if ((sequencer != null) && (sequencer.isOpen())) {
            sequencer.stop();
            // visualKaraoke.setVisible(false);
            sequencer.setTickPosition(0);
        }
    }

    public void pause() {
        if ((sequencer != null) && (sequencer.isOpen())) {
            sequencer.stop();
        }
    }

    public void resume() {
        play();
    }

    public static void main(String argv[]) {
        JavaSoundMidiKar plug = new JavaSoundMidiKar();
        plug.loadFile(new File("gaby.kar"));
        plug.play();
        while (true);
    }

    class Ticker extends Thread {
        public Ticker() {
            start();
        }

        public void run() {
            sequencer.getSequence().getTickLength();
            while (sequencer.isRunning()) {
                long pos = sequencer.getTickPosition();
                visualKaraoke.pulse(pos);
                // Here call listeners
                if (player != null) {
                    player.setCurrentPosition(pos);
                }
                try {
                    // CAREFUL, LOW VALUE HERE BREAKS THE SEEK !!!
                    sleep(200);
                }
                catch (Exception exn) {
                    exn.printStackTrace();
                }
            }
            System.out.println("sequencer is stopped");
        }
    }

    public long getSongLength() {
        return songLength;
    }

    public long seek(long bytes) {
        sequencer.setTickPosition(bytes);
        visualKaraoke.seek(bytes);
        return songLength - bytes;
    }

    public void setGain(double fGain) {
        currentGain = fGain;
        for (int i = 0; i < channels.length; i++) {
            channels[i].controlChange(7, (int) (fGain * 127.0));
        }
    }

    public void setPan(double fPan) {
        currentPan = fPan;
        System.out.println("Dans setPan val = " + (int) (fPan * 127.0));
        for (int i = 0; i < channels.length; i++) {
            channels[i].controlChange(10, (int) (fPan * 127.0));
        }
    }

    public long getSongMicrosecondLength() {
        return sequencer.getMicrosecondLength();
    }

    public String getArtist() {
        return artist;
    }

    public String getSongTitle() {
        return songTitle;
    }
}
