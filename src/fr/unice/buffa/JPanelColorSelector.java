package fr.unice.buffa;

import java.awt.*;
import javax.swing.JPanel;
import java.awt.event.*;
import javax.swing.JColorChooser;
import javax.swing.BorderFactory;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class JPanelColorSelector extends JPanel {
  BorderLayout borderLayout1 = new BorderLayout();


  public JPanelColorSelector() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.setBorder(BorderFactory.createLineBorder(Color.black));
    this.addMouseListener(new JPanelColorSelector_this_mouseAdapter(this));
    this.setLayout(borderLayout1);
  }
  public static void main(String[] args) {
    JPanelColorSelector JPanelColorSelector1 = new JPanelColorSelector();
  }

  void this_mouseClicked(MouseEvent e) {
    Color color = JColorChooser.showDialog(this, null, getBackground());
    setBackground(color);
  }

}

class JPanelColorSelector_this_mouseAdapter extends java.awt.event.MouseAdapter {
  JPanelColorSelector adaptee;

  JPanelColorSelector_this_mouseAdapter(JPanelColorSelector adaptee) {
    this.adaptee = adaptee;
  }
  public void mouseClicked(MouseEvent e) {
    adaptee.this_mouseClicked(e);
  }
}