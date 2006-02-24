package fr.unice.buffa;

import java.awt.*;
import javax.swing.JPanel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class BackgroundPanel extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();

  public BackgroundPanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    this.setLayout(borderLayout1);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    drawBackground((Graphics2D) g);
}

private void drawBackground(Graphics2D g2) {
  g2.setPaint(new GradientPaint(0, 0, Color.BLACK, getWidth(),
                                getHeight(), Color.BLUE));
  g2.fillRect(0, 0, getWidth(), getHeight());
}

}