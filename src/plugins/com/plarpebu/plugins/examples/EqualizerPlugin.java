package com.plarpebu.plugins.examples;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import com.plarpebu.plugins.sdk.FramePlugin;

/**
 * Equalizer
 */
public class EqualizerPlugin extends FramePlugin implements BasicPlayerListener, MouseListener
{

	private JSlider[] sliders = null;

	private DefaultBoundedRangeModel[] models = null;

	private float[] equalizer = null;

	private int scale = 100;

	private int bands = 32;

	private Container pane = null;

	private JPanel north = null;

	private JPanel south = null;

	/**
	 * Constructor
	 * 
	 * @throws HeadlessException
	 */
	public EqualizerPlugin() throws HeadlessException
	{
		super();
		initUI();
	}

	/**
	 * Initialize UI
	 */
	public void initUI()
	{

		pane = this.getContentPane();
		north = new JPanel();
		south = new JPanel();

		sliders = new JSlider[bands];
		models = new DefaultBoundedRangeModel[bands];
		int middle = bands / 2;

		for (int i = 0; i < middle; i++)
		{
			models[i] = new DefaultBoundedRangeModel(0, 1, -scale, scale + 1);
			sliders[i] = new JSlider(models[i]);
			sliders[i].setOrientation(JSlider.VERTICAL);
			sliders[i].setPreferredSize(new Dimension(15, 70));
			sliders[i].addMouseListener(this);
			sliders[i].setEnabled(false);
			north.add(sliders[i]);
		}
		for (int i = middle; i < bands; i++)
		{
			models[i] = new DefaultBoundedRangeModel(0, 1, -scale, scale + 1);
			sliders[i] = new JSlider(models[i]);
			sliders[i].setOrientation(JSlider.VERTICAL);
			sliders[i].setPreferredSize(new Dimension(15, 70));
			sliders[i].addMouseListener(this);
			sliders[i].setEnabled(false);
			south.add(sliders[i]);
		}

		pane.add(north, BorderLayout.NORTH);
		pane.add(south, BorderLayout.SOUTH);
		this.setTitle("Equalizer " + getVersion());
		this.setSize(350, 170);
		this.setSize(100, 200);

		// by defaults, will read the last pos, size etc...
		readPreferences();
	}

	public void opened(Object stream, Map properties)
	{
		if (properties.containsKey("audio.type"))
		{
			String audiotype = ((String) properties.get("audio.type")).toLowerCase();
			if (audiotype.toLowerCase().equals("mp3"))
			{
				for (int i = 0; i < bands; i++)
				{
					sliders[i].setEnabled(true);
				}
			}
			else
			{
				for (int i = 0; i < bands; i++)
				{
					sliders[i].setEnabled(false);
				}
			}
		}
	}

	public void stateUpdated(BasicPlayerEvent event)
	{}

	public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties)
	{
		if (properties.containsKey("mp3.equalizer"))
		{
			equalizer = (float[]) properties.get("mp3.equalizer");
		}
	}

	public void mouseEntered(MouseEvent e)
	{}

	public void mouseExited(MouseEvent e)
	{}

	public void mousePressed(MouseEvent e)
	{}

	public void mouseClicked(MouseEvent e)
	{}

	public void mouseReleased(MouseEvent e)
	{
		for (int i = 0; i < models.length; i++)
		{
			float val = (models[i].getValue() * 1.0f) / scale * 1.0f;
			equalizer[i] = val;
		}
	}

	public void setController(BasicController controller)
	{
	// this.controller = controller;
	}

	public String getName()
	{
		return "Equalizer";
	}

	/**
	 * getPlugin
	 * 
	 * @return BasicPlayerListener
	 */
	public BasicPlayerListener getPlugin()
	{
		return this;
	}

	/**
	 * setController
	 * 
	 * @param controller
	 *           BasicController
	 */

	public String getVersion()
	{
		return "v0.9";
	}

}
