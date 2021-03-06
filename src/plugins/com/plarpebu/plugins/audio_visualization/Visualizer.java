package com.plarpebu.plugins.audio_visualization;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Map;

import javax.swing.JPanel;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import com.plarpebu.plugins.sdk.FramePlugin;

/**
 * Visualizer
 * 
 * @author not attributable
 * @version 1.0
 */
public class Visualizer extends FramePlugin implements BasicPlayerListener, Runnable, ImageObserver
{

	private BorderLayout borderLayout1 = new BorderLayout();

	private JPanel jPanel1 = new JPanel();

	private Container pane = null;

	private int sound[];

	private byte pixels[];

	private int width;

	private int height;

	private Image img;

	private MemoryImageSource source;

	private static Thread thread;

	private static boolean stopThread = false;

	GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

	GraphicsDevice device = env.getDefaultScreenDevice();

	GraphicsConfiguration gc = device.getDefaultConfiguration();

	// private static DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[] { new DisplayMode(640,
	// 480, 32, 0),
	// new DisplayMode(640, 480, 16, 0), new DisplayMode(640, 480, 8, 0) };

	public Visualizer()
	{
		super("Visualizer");
		try
		{
			jbInit();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception
	{
		pane = this.getContentPane();
		pane.setLayout(borderLayout1);
		pane.add(jPanel1, BorderLayout.CENTER);

		addMouseListener(new MyMouseListener());

		width = 400;
		height = 200;

		setSize(width, height);

		sound = new int[width];

		byte reds[] = new byte[256];
		byte greens[] = new byte[256];
		byte blues[] = new byte[256];

		for (int i = 0; i < 64; i++)
		{
			reds[i] = (byte) (i * 4);
			greens[i + 64] = (byte) (i * 4);
			blues[i + 128] = (byte) (i * 4);
			reds[i + 192] = (byte) (i * 4);
			greens[i + 192] = (byte) (i * 4);
			blues[i + 192] = (byte) (i * 4);
		}

		pixels = new byte[getWidth() * getHeight()];
		IndexColorModel icm = new IndexColorModel(8, 256, reds, greens, blues);
		source = new MemoryImageSource(getWidth(), getHeight(), icm, pixels, 0, getWidth());
		source.setAnimated(true);
		img = createImage(source);
		prepareImage(img, this);
	}

	public void setController(BasicController controller)
	{}

	public BasicPlayerListener getPlugin()
	{
		return this;
	}

	public String getName()
	{
		return "Visu";
	}

	public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h)
	{
		if ((flags & 0x30) != 0)
		{
			repaint();
		}
		return (flags & 0xc0) == 0;
	}

	public void paint(Graphics g)
	{
		update(g);
	}

	public void update(Graphics g)
	{
		g.drawImage(img, 0, 0, this);
	}

	public void start()
	{
		// Must create a new thread each time, cannot restart a dead thread.
		thread = new Thread(this);
		stopThread = false;
		thread.start();
	}

	public void stop()
	{
		stopThread = true;
	}

	public void run()
	{
		// Thread.currentThread().setPriority(10);
		do
		{
			sound[0] = rand(-height / 3, height / 3);
			for (int i = 1; i < width; i++)
			{
				sound[i] = sound[i - 1] + rand(-10, 10);
				if (sound[i] < -height)
				{
					sound[i] += 2 * height;
				}
				if (sound[i] > height)
				{
					sound[i] -= 2 * height;
				}
			}

			int ib = (height - 1) * width;
			for (int i = 0; i < width; i++)
			{
				pixels[i] = 0;
				pixels[ib + i] = 0;
			}

			ib = 0;
			for (int i = 0; i < height; i++)
			{
				pixels[ib] = 0;
				pixels[(ib + width) - 1] = 0;
				ib += width;
			}

			for (int i = 1; i < (height - 1) * (width - 1); i++)
			{
				int p1 = pixels[i + 1];
				int p2 = pixels[i + width];
				int p3 = pixels[i + width + 1];
				int p4 = pixels[i];
				if (p1 < 0)
				{
					p1 += 256;
				}
				if (p2 < 0)
				{
					p2 += 256;
				}
				if (p3 < 0)
				{
					p3 += 256;
				}
				if (p4 < 0)
				{
					p4 += 256;
				}
				int s = p1 + p2 + p3 + p4;
				s /= 4;
				if (s > 0)
				{
					s--;
				}
				pixels[i] = (byte) s;
			}

			for (int i = 0; i < width; i++)
			{
				pixels[(height / 2 + sound[i] / 3) * width + i] = -1;
			}

			source.newPixels();

			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException ex)
			{}

			// If told to stop, kill the thread
			if (stopThread)
				break;
		}
		while (true);
	}

	static int rand(int low, int high)
	{
		return (int) (low + Math.random() * (high - low));
	}

	class MyMouseListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				System.out.println("on a double click�");
			}
		}
	}

	/**
	 * opened
	 * 
	 * @param stream
	 *           Object
	 * @param properties
	 *           Map
	 */
	public void opened(Object stream, Map properties)
	{
	// start();
	}

	/**
	 * progress
	 * 
	 * @param bytesread
	 *           int
	 * @param microseconds
	 *           long
	 * @param pcmdata
	 *           byte[]
	 * @param properties
	 *           Map
	 */
	public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties)
	{}

	/**
	 * stateUpdated
	 * 
	 * @param event
	 *           BasicPlayerEvent
	 */
	public void stateUpdated(BasicPlayerEvent event)
	{
		if (event.getCode() == BasicPlayerEvent.STOPPED || event.getCode() == BasicPlayerEvent.EOM)
		{
			stop();
		}
		if (event.getCode() == BasicPlayerEvent.PLAYING)
		{
			start();
		}
	}

	public String getVersion()
	{
		return "v1.0";
	}

}
