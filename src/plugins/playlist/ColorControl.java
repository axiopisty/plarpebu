package plugins.playlist;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


public class ColorControl extends JPanel
{

  private Tools tool = null;

    public ColorControl(Tools t)
    {
        setBorder(DefaultBorder);
        enableEvents(16L);
        setOpaque(true);
        setBackground(null);
        tool = t;

    }


    public ColorControl(boolean flag)
    {
      super();
      setAllowsGradient(flag);
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(30, 20);
    }

    public void setAllowsGradient(boolean flag)
    {
        allowsGradient = flag;
    }

    public boolean getAllowsGradient()
    {
        return allowsGradient;
    }

    public void setBackgroundPaint(Paint paint)
    {
        Paint paint1 = backgroundPaint;
        backgroundPaint = paint;
        firePropertyChange("backgroundPaint", paint1, paint);
        repaint();
    }

    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }

    public Color getBackground()
    {
        Paint paint = getBackgroundPaint();
        if(paint instanceof Color)
            return (Color)paint;
        else
            return super.getBackground();
    }

    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }

    protected void paintComponent(Graphics g)
    {
        Paint paint = getBackgroundPaint();
        if(paint == null || (paint instanceof Color))
        {
            super.paintComponent(g);
        } else
        {
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            Graphics g1 = g.create();
            try
            {
                Graphics2D graphics2d = (Graphics2D)g1;
                Insets insets = getInsets();
                graphics2d.translate(insets.left, insets.right);
                graphics2d.scale(getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);
                graphics2d.setPaint(paint);
                graphics2d.fillRect(0, 0, 1, 1);
            }
            finally
            {
                g1.dispose();
            }
        }
    }

    protected void processMouseEvent(MouseEvent mouseevent)
    {
        super.processMouseEvent(mouseevent);
        if(isEnabled() && !mouseevent.isConsumed())
        {
            int i = mouseevent.getID();
            switch(i)
            {
            case 501:
                Paint paint = getBackgroundPaint();


                    Color color = JColorChooser.showDialog(this, null, getBackground());
                    if(color != null){
                      tool.setChange(true);
                      setBackgroundPaint(color);

                    }

                break;
            }
        }
    }

    static Border DefaultBorder;
    private boolean allowsGradient;
    private Paint backgroundPaint;

    static
    {
        CompoundBorder compoundborder = new CompoundBorder(new LineBorder(Color.lightGray, 2), new BevelBorder(1, Color.white, Color.darkGray));
        DefaultBorder = new CompoundBorder(new BevelBorder(0, Color.white, Color.darkGray), compoundborder);
    }
}
