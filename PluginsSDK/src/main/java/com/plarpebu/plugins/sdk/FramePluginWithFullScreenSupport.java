package com.plarpebu.plugins.sdk;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JRootPane;

/**
 * This class adds to the FramePlugin class support for hardware fullscreen mode. This mode can be
 * entered by double clicking on the Jframe.
 * 
 * @author not attributable
 * @version 1.0
 */
abstract public class FramePluginWithFullScreenSupport extends JFrameWithPreferences
{
	private boolean windowedMode = true;

	private boolean hardwareAcceleratedFullScreenModeSupported = true;

	private int oldPosX;

	private int oldPosY;

	private int oldWidth;

	private int oldHeight;

	DisplayMode userDisplayMode = null;

	private static DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[]
	{ new DisplayMode(640, 480, 32, 0), new DisplayMode(640, 480, 16, 0),
	         new DisplayMode(640, 480, 8, 0) };

	private GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

	private GraphicsDevice myDevice = env.getDefaultScreenDevice();

	private DisplayMode oldDisplayMode = myDevice.getDisplayMode();

	private int oldWindowDecorationStyle;

	/**
	 * Constructor
	 */
	public FramePluginWithFullScreenSupport(String title)
	{
		super(title);
		
		// Mouse listener for double click detection
		addMouseListener(new MyMouseListener());
	}

	public class MyMouseListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				// Switching to/from fullScreen/windowed mode
				switchFullScreenWindowedMode();
			}
		}
	}

	private void switchFullScreenWindowedMode()
	{
		if (windowedMode)
		{
			System.out.println("Go TO FULL SCREEN");
			// Go to full screen
			try
			{
				// go fullscreen
				goToFullScreen();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				goToWindowedMode();
			}
		}
		else
		{
			goToWindowedMode();
		}
	}

	protected void goToFullScreen()
	{
		if (!hardwareAcceleratedFullScreenModeSupported)
			return;

		// Store old pos and size
		oldPosX = getX();
		oldPosY = getY();
		oldWidth = getWidth();
		oldHeight = getHeight();

		if (windowedMode)
		{
			// No borders, close buttons etc...
			// displayDecorations(false);
			// remove all decorations before going full screen
			oldWindowDecorationStyle = getRootPane().getWindowDecorationStyle();

			getRootPane().setWindowDecorationStyle(JRootPane.NONE);
			// displayDecorations(false);
			setIgnoreRepaint(true);
			myDevice.setFullScreenWindow(FramePluginWithFullScreenSupport.this);
			if (myDevice.isDisplayChangeSupported())
			{
				if (userDisplayMode == null)
				{
					chooseBestDisplayMode(myDevice);
				}
				else
				{
					myDevice.setDisplayMode(userDisplayMode);

				}
			}
			// we can't modify full screen options while in full screen
			windowedMode = false;
		}
	}

	public void displayDecorations(boolean flag)
	{
		dispose();
		// must be displayable (disposed) to call this...
		setUndecorated(!flag);
		pack();
	}

	public static void chooseBestDisplayMode(GraphicsDevice device)
	{
		DisplayMode best = getBestDisplayMode(device);
		if (best != null)
		{
			device.setDisplayMode(best);
		}
	}

	private static DisplayMode getBestDisplayMode(GraphicsDevice device)
	{
		for (int x = 0; x < BEST_DISPLAY_MODES.length; x++)
		{
			DisplayMode[] modes = device.getDisplayModes();
			for (int i = 0; i < modes.length; i++)
			{
				if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth()
				         && modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight()
				         && modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth())
				{
					return BEST_DISPLAY_MODES[x];
				}
			}
		}
		return null;
	}

	protected void goToWindowedMode()
	{
		if (!hardwareAcceleratedFullScreenModeSupported)
			return;

		if (!windowedMode)
		{
			// Go back to the old display mode, windowed, with decorations...
			myDevice.setDisplayMode(oldDisplayMode);
			myDevice.setFullScreenWindow(null);
			// panelLyrics.setWindowedMode(true);
			// displayDecorations(true);

			// restore decorations
			getRootPane().setWindowDecorationStyle(oldWindowDecorationStyle);
			// displayDecorations(true);

			restorePositionAndSize();
			setIgnoreRepaint(false);

			setVisible(true);
			// !!!

			// de-iconify the main interface of the player
			// controllerMp3.getPlayerUI().setToOriginalSize();

			// setUndecorated(true);
			// we can only modify full screen options while in window mode
			windowedMode = true;
		}
	}

	private void restorePositionAndSize()
	{
		// no hardware acceleration mode, just restore the window to its
		// previous pos and size
		setLocation(oldPosX, oldPosY);
		setSize(oldWidth, oldHeight);
	}

}
