package segments_clock;

import javax.swing.JPanel;
import java.awt.LayoutManager;
import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SevenSegClockPanel extends JLabel implements Runnable{
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
    while(true) {
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
    SevenSegClockPanel sevenSegClock1 = new SevenSegClockPanel();
  }

  public void setTimeToDisplay(String time) {
    sevenSegClock.setDisplayValue(time);

  }
}