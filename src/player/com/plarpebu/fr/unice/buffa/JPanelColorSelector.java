package fr.unice.buffa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JPanel;

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
 * @author not attributable
 * @version 1.0
 */
public class JPanelColorSelector extends JPanel {
    BorderLayout borderLayout1 = new BorderLayout();

    public JPanelColorSelector() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.addMouseListener(new JPanelColorSelector_this_mouseAdapter(this));
        this.setLayout(borderLayout1);
    }

    void this_mouseClicked(MouseEvent e) {
        Color color = JColorChooser.showDialog(this, null, getBackground());
        setBackground(color);
    }

}

class JPanelColorSelector_this_mouseAdapter extends MouseAdapter {
    JPanelColorSelector adaptee;

    JPanelColorSelector_this_mouseAdapter(JPanelColorSelector adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.this_mouseClicked(e);
    }
}