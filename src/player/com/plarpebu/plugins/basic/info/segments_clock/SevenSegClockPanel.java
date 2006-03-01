package com.plarpebu.plugins.basic.info.segments_clock;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Seven Segment Clock
 * 
 * @author not attributable
 * @version 1.0
 */
public class SevenSegClockPanel extends JLabel implements Runnable {
    private SevenSegDigit sevenSegClock;

    public SevenSegClockPanel() {
        sevenSegClock = new SevenSegDigit();

        sevenSegClock.setNumDigits(6);
        sevenSegClock.setDisplayValue("00:00:00");
        new Thread(this).start();
    }

    public Dimension getPreferredSize() {
        return new Dimension(100, 30);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void run() {
        while (true) {
            repaint();
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        sevenSegClock.Xsize = getWidth();
        sevenSegClock.Ysize = getHeight();

        sevenSegClock.PaintSevenSegDisplay(g, false, this);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        SevenSegClockPanel sevenSegClock1 = new SevenSegClockPanel();
        sevenSegClock1.setTimeToDisplay("00:01:02");
        frame.add(sevenSegClock1);
        frame.setTitle("Seven Seg Clock Test");
        frame.setSize(400,200);
        frame.setVisible(true);
    }

    public void setTimeToDisplay(String time) {
        sevenSegClock.setDisplayValue(time);
    }
}