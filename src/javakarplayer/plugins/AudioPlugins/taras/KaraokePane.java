package javakarplayer.plugins.AudioPlugins.taras;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * Karaoke text panel. Panel where karaoke text is drawn, highlighted and pulse
 * ribbon jumps. 03 July 2002
 * 
 * @author Taras P. Galchenko
 */
class KaraokePane extends JPanel {
    Vector song = null;

    Vector frames = null;

    KaraokeProperties props = null;

    Font font = null, cr_font = null;

    int currentFrame = 0;

    long currentTick = -1;

    Vector lines;

    boolean discontinuity = false;

    // for ribbon redisplay
    double sx, sy;

    int imageWidth, imageHeight;

    // For rich text display
    private FontRenderContext frc;

    private Shape sha = null;

    private TextLayout textlay = null;

    private FontMetrics fm;

    // For transparent shadows
    AlphaComposite ac_transparent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    AlphaComposite ac_solid = AlphaComposite.getInstance(AlphaComposite.SRC);

    /**
     * Creates panel and sets size taken from properties object this properties
     * are not stored, you should pass correct properties to setSong
     * 
     * @param p
     *        width and height of the panel
     */
    private BufferedImage backgroundImage;

    private BufferedImage finalImage;

    private Graphics finalImageGraphics;

    private Graphics2D backgroundImageGraphics;

    GridLayout gridLayout1 = new GridLayout();

    // for the damaged area
    private Rectangle damagedRectangle = new Rectangle();

    public KaraokePane(KaraokeProperties p) {
        super();
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        setSize(p.width, p.height);
        setDoubleBuffered(true);

        // setIgnoreRepaint(true);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                props.width = getWidth();
                props.height = getHeight();
                /* next time it is painted, it will be recomputed */
                font = null;
                finalImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                finalImageGraphics = finalImage.getGraphics();
                // draw background
                // drawBackground( (Graphics2D) finalImageGraphics);
                discontinuity = true;
                redrawAllAfterResize();
                // System.out.println("resize");
            }
        });
    }

    /**
     * Finds the maximum length of the text lines
     * 
     * @param fm
     *        font metrics to use to measure text
     * @return the maximum length
     */
    int maxLength(FontMetrics fm) {
        int len, max_len = 0;

        for (int i = 0; i < lines.size(); ++i) {
            len = fm.stringWidth((String) lines.elementAt(i));
            if (len > max_len) max_len = len;
        }

        return max_len;
    }

    /**
     * Attempts to find font so that whole text (lines x cols) should fit into
     * current panel dimensions. The font is saved to the 'font' class member.
     * On error error is thrown
     * 
     * @param g
     *        graphics context for painting
     */
    void chooseFont(Graphics g) {
        boolean quit = false;
        int best = 0;

        for (int size = 4; !quit; ++size) {
            Font f = new Font(props.fontFace, props.style, size);
            FontMetrics fm = g.getFontMetrics(f);
            if (props.height >= props.lines * (fm.getHeight() + fm.getLeading()) + 2 * fm.getHeight()) {
                best = size;
            }
            else {
                quit = true;
            }

            if (!quit && props.width >= maxLength(fm) + 2 * fm.charWidth('A')) {
                best = size;
            }
            else {
                quit = true;
            }
        }

        if (0 == best) {
            throw new Error("Can't find fitting font");
        }

        font = new Font(props.fontFace, props.style, best);
        cr_font = new Font(props.fontFace, props.style, best / 3 == 0 ? 3 : best / 3);
    }

    /**
     * Attaches the given song and frames to the panel and adopts given
     * properties.
     * 
     * @param s
     *        song (vector of syllables)
     * @param f
     *        frames (vector of frames)
     * @param p
     *        karaoke properties
     */
    public void setSong(Vector s, Vector f, KaraokeProperties p) {
        // System.out.println("=====SetSong====");
        song = s;
        frames = f;
        props = p;
        font = null;
        currentFrame = 0;
        lines = song != null ? Lyrics.makeText(song) : null;

        /*
         * if (props.bgImage == null) { setBackground(props.bgColor); }
         */

        discontinuity = true;
        // render();
    }

    /**
     * Redraws whole frame
     * 
     * @param g
     *        graphics context to draw in
     * @param frame
     *        frame to draw
     */
    void paint(Graphics g, KFrame frame) {

        // draw lyrics that's sung

        Graphics2D g2 = (Graphics2D) g;

        if (font == null) {
            chooseFont(g);
        }

        g.setFont(font);
        // Do some computations on text
        frc = g2.getFontRenderContext();
        fm = g.getFontMetrics(font);

        if (frame.scrolled || discontinuity) {
            // Redraw background
            drawBackground(g2);
        }

        int dx = fm.charWidth('A'), dy = fm.getHeight() + fm.getLeading();
        int x = dx, y = 3 * dy / 2;
        int line = frame.first_line;

        // g.setColor(props.hlColor);

        for (int i = 0; i < frame.sung.size(); ++i) {
            Syllable syl = (Syllable) frame.sung.elementAt(i);
            while (line < syl.line) {
                y += dy;
                x = dx;
                ++line;
            }

            // Draw sung text
            drawRichText(g2, x, y, syl.text, props.sungSyllabesColor, true);

            // g2.drawString(syl.text, x, y);
            x += fm.stringWidth(syl.text);
        }

        // g.setColor(props.fgColor);

        for (int i = 0; i < frame.clear.size(); ++i) {
            Syllable syl = (Syllable) frame.clear.elementAt(i);
            while (line < syl.line) {
                y += dy;
                x = dx;
                ++line;
            }

            // Draw normal text
            drawRichText(g2, x, y, syl.text, props.syllabesNotSungYetColor, true);

            x += fm.stringWidth(syl.text);
        }

        g.setColor(props.syllabesNotSungYetColor);
        g.setFont(cr_font);
        /* copyright notice */
        g.drawString("Original author : Taras P. Galchenko, many improvements by M.Buffa...", fm.charWidth('A'),
        props.height - fm.getHeight() / 2);

        /* pulsation ribbon */
        double c = currentTick == -1 ? 0.6 : 0.6 * (frame.end - currentTick) / (frame.end - frame.start);
        x = props.width / 5;
        y = (fm.getHeight() - props.ribbonWidth) >> 1;
        g.setColor(props.ribbonColor);
        g.fillRect(x, y, (int) (c * props.width), props.ribbonWidth);
    }

    private void drawBackground(Graphics2D g2) {
        if (props.backgroundType == KaraokeProperties.BACKGROUND_PLAIN) {
            g2.setColor(props.bgColor);
            g2.fillRect(0, 0, props.width, props.width);
        }
        else {
            if (props.backgroundType == KaraokeProperties.BACKGROUND_GRADIANT) {
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
                else {
                    createGradiantBackgroundImage();
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
            else if (props.backgroundType == KaraokeProperties.BACKGROUND_IMAGE) {
                if ((props.bgImage != null) && (!props.bgImageFilename.equals("noImage"))
                && (props.bgImage.getWidth(this) != 0)) {
                    g2.drawImage(props.bgImage, 0, 0, getWidth(), getHeight(), this);
                }
                else {
                    g2.setColor(props.bgColor);
                    g2.fillRect(0, 0, props.width, props.width);
                }
            }
        }
    }

    private void createGradiantBackgroundImage() {
        backgroundImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        backgroundImageGraphics = backgroundImage.createGraphics();
        repaintGradiantBackgroundImage();
    }

    private void repaintGradiantBackgroundImage() {
        backgroundImageGraphics.setPaint(new GradientPaint(0, 0, props.startGradiantColor, props.width, props.height,
        props.endGradiantColor));
        backgroundImageGraphics.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawRichText(Graphics2D g2, int x, int y, String text, Color fillColor, boolean drawShadow) {
        try {
            if (font == null) chooseFont(g2);

            textlay = new TextLayout(text, font, frc);
            sha = textlay.getOutline(AffineTransform.getTranslateInstance(x, y));

            // Draw shadow
            if (drawShadow) {
                if (props.displayShadow) {
                    // No antialiasing for shadows
                    setAntiAliasing(g2, false);
                    sha = textlay.getOutline(AffineTransform.getTranslateInstance(x + 4, y + 4));
                    // transparent shadow ?
                    g2.setComposite(ac_transparent);
                    g2.setColor(props.shadowColor);
                    g2.fill(sha);
                    // draw solid again
                    g2.setComposite(ac_solid);
                }
            }

            // maybe antialiase text ?
            setAntiAliasing(g2, props.antiAliasedText);

            // Get text shape
            sha = textlay.getOutline(AffineTransform.getTranslateInstance(x, y));
            if (sha != null) {
                // display outline ?
                if (props.displayOutline) {
                    // g2.setStroke(new BasicStroke(props.outlineWidth));
                    g2.setStroke(new BasicStroke(props.outlineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(props.outlineColor);
                    g2.draw(sha);
                }

                // Draw filled text
                g2.setColor(fillColor);
                g2.fill(sha);

                damagedRectangle.x = (int) sha.getBounds().getX();
                damagedRectangle.y = (int) sha.getBounds().getY();
                damagedRectangle.width = (int) sha.getBounds().getWidth();
                damagedRectangle.height = (int) sha.getBounds().getHeight();
            }

            // switch off antialiased so that only text is displayed
            // anti-aliased !
            setAntiAliasing(g2, false);
        }
        catch (Exception e) {
            System.out.println("problem in KaraokePane : drawRichText");
        }
    }

    /**
     * Paints just the the highlighted syllables of the frame. This method
     * should be called to update visible state
     * 
     * @param g
     *        graphics context to draw in
     * @param frame
     *        frame to draw
     */
    void paintSyllable(Graphics g, KFrame frame) {
        String oldText = null;
        int oldX = 0, oldY = 0;

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);

        int dx = fm.charWidth('A'), dy = fm.getHeight() + fm.getLeading();
        int x = dx, y = 3 * dy / 2;
        int line = frame.first_line;

        Graphics2D g2 = (Graphics2D) g;

        // System.out.println("--------- entree paint syllabe ----------");
        for (int i = 0; i < frame.sung.size(); ++i) {
            Syllable syl = (Syllable) frame.sung.elementAt(i);
            // System.out.println("syllabe de frame.sung : " + syl.text);
            while (line < syl.line) {
                y += dy;
                x = dx;
                ++line;
            }

            // unfortunately we can't be sure that paintSyllable is
            // called once for every syllable :(
            // from time to time several calls to repaint are merged
            // into one call to update
            // g2.drawString(syl.text, x, y);

            if (oldText != null) {
                // System.out.println("paint syllabe j'efface : " + oldText);
                drawRichText(g2, oldX, oldY, oldText, props.sungSyllabesColor, false);
                redrawPartialImage();
            }

            if (i + 1 == frame.sung.size()) {

                // System.out.println("paint syllabe je dessine : " + syl.text);
                drawRichText(g2, x, y, syl.text, props.syllabeToSingColor, false);
                redrawPartialImage();
            }
            oldText = syl.text;
            oldX = x;
            oldY = y;

            x += fm.stringWidth(syl.text);
        }

        /* pulsation ribbon */
        x = props.width / 5;
        y = (fm.getHeight() - props.ribbonWidth) >> 1;
        g2.setColor(props.ribbonColor);
        g2.fillRect(x, y, 3 * props.width / 5, props.ribbonWidth);
        // just redraw the damaged part
        damagedRectangle.x = x;
        damagedRectangle.y = y;
        damagedRectangle.width = 3 * props.width / 5;
        damagedRectangle.height = props.ribbonWidth;

        redrawPartialImage();
    }

    private void setAntiAliasing(Graphics g, boolean flag) {
        Graphics2D g2 = (Graphics2D) g;

        if (flag) {
            // Activate anti aliasing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    /**
     * Wipes away tail of the ribbon
     * 
     * @param g
     *        graphics context to draw in
     * @param frame
     *        current frame
     */
    void paintRibbon(Graphics g, KFrame frame) {
        g = (Graphics2D) g;

        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);

        int y = (fm.getHeight() - props.ribbonWidth) >> 1;
        double c = 0.6 * (frame.end - currentTick) / (frame.end - frame.start);
        int x = (int) (c * props.width);
        int w = 4 * props.width / 5 - x;
        x += props.width / 5;

        damagedRectangle.x = x;
        damagedRectangle.y = y;
        damagedRectangle.width = w;
        damagedRectangle.height = props.ribbonWidth;

        if (props.backgroundType == KaraokeProperties.BACKGROUND_GRADIANT) {
            imageWidth = backgroundImage.getWidth(this);
            imageHeight = backgroundImage.getHeight(this);
            sx = (imageWidth / (double) props.width);
            sy = (imageHeight / (double) props.height);
            g.drawImage(backgroundImage, x, y, x + w, y + props.ribbonWidth, (int) (sx * x), (int) (sy * y),
            (int) (sx * (x + w)), (int) (sy * (y + props.ribbonWidth)), this);
        }
        else if (props.backgroundType == KaraokeProperties.BACKGROUND_PLAIN) {
            g.setColor(props.bgColor);
            g.fillRect(x, y, w, props.ribbonWidth);
        }
        else if (props.backgroundType == KaraokeProperties.BACKGROUND_IMAGE) {
            imageWidth = props.bgImage.getWidth(this);
            imageHeight = props.bgImage.getHeight(this);
            sx = (imageWidth / (double) props.width);
            sy = (imageHeight / (double) props.height);

            g.drawImage(props.bgImage, x, y, x + w, y + props.ribbonWidth, (int) (sx * x), (int) (sy * y),
            (int) (sx * (x + w)), (int) (sy * (y + props.ribbonWidth)), this);
        }

        redrawPartialImage();
    }

    private void redrawPartialImage() {
        // finalImageGraphics.draw3DRect(damagedRectangle.x, damagedRectangle.y,
        // damagedRectangle.width, damagedRectangle.height, true);

        // paintImmediately(damagedRectangle);
        // repaint();
        getGraphics().drawImage(finalImage, damagedRectangle.x, damagedRectangle.y,
        damagedRectangle.x + damagedRectangle.width, damagedRectangle.y + damagedRectangle.height, damagedRectangle.x,
        damagedRectangle.y, damagedRectangle.x + damagedRectangle.width, damagedRectangle.y + damagedRectangle.height,
        this);

    }

    public void paintComponent(Graphics g) {
        // System.out.println("paint");
        g.drawImage(finalImage, 0, 0, getWidth(), getHeight(), this);
    }

    private void redrawAllAfterResize() {
        if (finalImageGraphics != null) {
            drawWholeBufferWithoutDisplayingIt();
            // paintComponent will be called after this as we have been called
            // by a resize
            // listener
        }
    }

    /**
     * Called in response to repaint () call. Updates visible content to
     * correspond to the internal state (current song position)
     * 
     * @param g
     *        graphics context to draw in
     */
    public synchronized void render() {
        // System.out.println("--start render--");
        Graphics g = getGraphics();
        Graphics g1 = finalImageGraphics;

        if (g == null) {
            System.out.println("G = NULL");
        }

        if (song == null) return;

        if (font == null) chooseFont(g);

        KFrame frame = (KFrame) frames.elementAt(currentFrame);
        if (!discontinuity && currentTick != -1) {
            paintRibbon(g1, frame);
        }
        else if (frame.scrolled || discontinuity) {
            if (g1 != null) {
                paint(finalImageGraphics, frame);
                // paintImmediately(0, 0, getWidth(), getHeight());
                // repaint();
                getGraphics().drawImage(finalImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
        else {
            paintSyllable(g1, frame);
        }
        // System.out.println("--end render--");
    }

    /**
     * Called by Ticker object to signal that ribbon should be shortened. If the
     * position falls into current frame, update is requested.
     * 
     * @param tick
     *        current song position
     */
    public void pulse(long tick) {
        KFrame frame = (KFrame) frames.elementAt(currentFrame);
        if (frame.start < tick && tick < frame.end) {
            currentTick = tick;
            // repaint();
            render();
        }
    }

    /**
     * Called by MIDI meta event listener when meta event is encountered. Frame
     * corresponding to the given position is searched and then update is
     * requested.
     * 
     * @param tick
     *        current song position
     */
    public void seek(long tick, boolean disc) {
        discontinuity = disc;
        currentTick = discontinuity ? tick : -1;
        KFrame frame = null;

        /* small optimization */
        if (currentFrame < frames.size()) {
            frame = (KFrame) frames.elementAt(currentFrame);
            if (frame.start <= tick && tick < frame.end) {
                // System.out.println("SMALL OPTI");
                // repaint();
                render();
                return;
            }
        }

        if (currentFrame + 1 < frames.size()) {
            frame = (KFrame) frames.elementAt(currentFrame + 1);
            // DANGER !!!! If we do not have enough time to keep the frame rate,
            // then
            // drawing one frame can take too long and we will have problems.
            // In that case, the comment should be removed
            if (frame.start <= tick && tick < frame.end) {
                // System.out.println("JE PASSE 0 LA FRAME SUIVANTE");
                ++currentFrame;
                // repaint();
                render();
                return;
            }
        }
        // System.out.println("Je vais dans FULL SEARCH frame.start = " +
        // frame.start + " " + "tick = " + tick + "frame.end = " + frame.end);
        /* full search of the frame */
        for (int i = 0; i < frames.size(); ++i) {
            frame = (KFrame) frames.elementAt(i);
            if (frame.start <= tick && tick < frame.end) {
                // System.out.println("FULL SEARCH frame.start = " + frame.start
                // + " " + "tick = " + tick + "frame.end = " + frame.end);
                currentFrame = i;
                // repaint();
                render();
                return;
            }
        }
    }

    public Font getCurentFont() {
        return font;
    }

    public void setCurentFont(Font f) {
        String name = f.getFontName();
        int style = f.getStyle();
        System.out.println("police choisie : " + name);
        props.fontFace = name;
        props.style = style;

        // hack so that in the paint(), choseFont will be called
        font = null;
        // repaint();
        render();
    }

    public void redrawAll() {
        drawWholeBufferWithoutDisplayingIt();
        // getGraphics().drawImage(finalImage, 0, 0, getWidth(), getHeight(),
        // this);
        // repaint();
        // getGraphics().drawImage(finalImage, 0, 0, getWidth(), getHeight(),
        // this);
    }

    private void drawWholeBufferWithoutDisplayingIt() {
        KFrame frame = (KFrame) frames.elementAt(currentFrame);
        discontinuity = true; // force background redisplay
        paint(finalImageGraphics, frame);
        paintRibbon(finalImageGraphics, frame);
        paintSyllable(finalImageGraphics, frame);
        System.out.println("drawWholeBufferWithoutDisplayingIt");
        repaint();
    }

    public void setActiveColor(Color c) {
        props.sungSyllabesColor = c;
        redrawAll();
    }

    public Color getActiveColor() {
        return props.sungSyllabesColor;
    }

    public void setHlColor(Color c) {
        props.syllabeToSingColor = c;
        redrawAll();
    }

    public Color getHlColor() {
        return props.syllabeToSingColor;
    }

    public void setFgColor(Color c) {
        props.syllabesNotSungYetColor = c;
        redrawAll();
    }

    public Color getFgColor() {
        return props.syllabesNotSungYetColor;
    }

    public void setShadowColor(Color c) {
        props.shadowColor = c;
        redrawAll();
    }

    public Color getShadowColor() {
        return props.shadowColor;
    }

    public void setOutlineColor(Color c) {
        props.outlineColor = c;
        redrawAll();
    }

    public Color getOutlineColor() {
        return props.outlineColor;
    }

    public void setGradiantStartColor(Color c) {
        props.startGradiantColor = c;
        // repaintBackgroundImage();
        redrawAll();
    }

    public Color getGradiantStartColor() {
        return props.startGradiantColor;
    }

    public void setGradiantEndColor(Color c) {
        props.endGradiantColor = c;
        // repaintBackgroundImage();
        redrawAll();
    }

    public Color getGradiantEndColor() {
        return props.endGradiantColor;
    }

    public void setMonochromeColor(Color c) {
        props.bgColor = c;
        // repaintBackgroundImage();
        redrawAll();
    }

    public Color getMonochromeColor() {
        return props.bgColor;

    }

    public void setAntialiasing(boolean b) {
        props.antiAliasedText = b;
    }

    public KaraokePane() {
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(gridLayout1);
        this.setForeground(Color.pink);
        this.setOpaque(false);
    }

}
