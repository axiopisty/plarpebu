package plugins.karaoke.cdg.lyricspanelCanvas;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: In the CD+G system, 16 color graphics are displayed on a raster
 * field which is 300 x 216 pixels in size. The middle 294 x 204 area is within
 * the TV's "safe area", and that is where the graphics are displayed. The outer
 * border is set to a solid color. The colors are stored in a 16 entry color
 * lookup table.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Michel Buffa (buffa@unice.fr)
 * @version 1.0
 */

public class CdgGraphicBufferedImage extends Canvas {

    // Colors that composes the indexed color model. 16 colors...
    byte[] reds = new byte[16];

    byte[] greens = new byte[16];

    byte[] blues = new byte[16];

    IndexColorModel icm;

    Image img;

    byte[] pixels = new byte[300 * 216];

    MemoryImageSource pic;

    public CdgGraphicBufferedImage() {
        // We do create a default colormodel with color 0 everywhere...
        setDefaultColormap();
        icm = new IndexColorModel(4, 16, reds, greens, blues);

        // Clear the initial screen
        Arrays.fill(pixels, (byte) 0);

        // Create the picture...
        pic = new MemoryImageSource(300, 216, icm, pixels, 0, 300);
        pic.setAnimated(true);
        img = createImage(pic);

        setSize(300, 216);
        // setPreferredSize(new Dimension(300, 216));
    }

    private void setDefaultColormap() {
        for (int i = 0; i < reds.length; i++) {
            reds[i] = (byte) 0;
            greens[i] = (byte) 0;
            blues[i] = (byte) 0;
        }
    }

    public void setColormapHigh(Color[] colormap) {
        setColormap(colormap, 8, 16);
    }

    public void setColormapLow(Color[] colormap) {
        setColormap(colormap, 0, 8);
    }

    private void setColormap(Color[] colormap, int startIndex, int stopIndex) {
        for (int i = startIndex; i < stopIndex; i++) {
            reds[i] = (byte) colormap[i].getRed();
            greens[i] = (byte) colormap[i].getGreen();
            blues[i] = (byte) colormap[i].getBlue();
        }

        icm = new IndexColorModel(4, 16, reds, greens, blues);

        pic.newPixels(pixels, icm, 0, 300);
        getGraphics().drawImage(img, 0, 0, this);
    }

    public void pixelsChanged() {
        pic.newPixels();
        getGraphics().drawImage(img, 0, 0, this);
    }

    public void pixelsChanged(Rectangle r) {
        pic.newPixels(r.x, r.y, r.width, r.height);
        getGraphics().drawImage(img, r.x, r.y, r.x + r.width, r.y + r.height, r.x, r.y, r.x + r.width, r.y + r.height,
        this);
    }

    public void paint(Graphics g) {}

    public void update(Graphics g) {}

    public byte[] getPixels() {
        return pixels;
    }

    public void setWindowedMode(boolean windowedMode) {

    }

    public void redrawFullImage() {

    }

    public void setForceDrawFullImage(boolean flag) {

    }

}
