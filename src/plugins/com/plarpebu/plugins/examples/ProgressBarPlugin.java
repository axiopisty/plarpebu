package com.plarpebu.plugins.examples;

import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.util.Map;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import com.plarpebu.plugins.sdk.FramePlugin;

/**
 * Progress Bar
 */
public class ProgressBarPlugin extends FramePlugin implements BasicPlayerListener, Runnable
{

	private JPanel panel = null;

	private JSlider slider = null;

	private JLabel positionTF = null;

	private JLabel positionLB = null;

	private JLabel lengthLB = null;

	private JLabel timeLB = null;

	private JLabel tpositionLB = null;

	private JLabel realtpositionLB = null;

	private JLabel realtimeLB = null;

	private JLabel pclockLB = null;

	private JLabel clockLB = null;

	private DefaultBoundedRangeModel model = null;

	private int modelscale = 1000;

	private Container pane = null;

	// Audio
	private int byteslength = -1;

	private long elapsed = -1;

	private int milliseconds = -1;

	// Clock Thread
	private boolean paused = false;

	private boolean running = false;

	private long clock = 0;

	private Boolean lock = Boolean.TRUE;

	public ProgressBarPlugin(/* String title */) throws HeadlessException
	{
		// super(title);
		initUI();
		setTitle("Progress Bar " + getVersion());
	}

	public void initUI()
	{

		panel = new JPanel();
		model = new DefaultBoundedRangeModel(0, 1, 0, modelscale);
		// Frames
		positionLB = new JLabel("Byte Position : ");
		positionLB.setBounds(new Rectangle(10, 10, 90, 20));
		positionTF = new JLabel("0");
		positionTF.setBounds(new Rectangle(100, 10, 100, 20));
		lengthLB = new JLabel(" / ");
		lengthLB.setBounds(new Rectangle(200, 10, 80, 20));
		tpositionLB = new JLabel("Time Position : ");
		tpositionLB.setBounds(new Rectangle(10, 30, 90, 20));
		timeLB = new JLabel("hh:mm:ss");
		timeLB.setBounds(new Rectangle(100, 30, 200, 20));
		realtpositionLB = new JLabel("Time Elapsed : ");
		realtpositionLB.setBounds(new Rectangle(10, 50, 90, 20));
		realtimeLB = new JLabel("hh:mm:ss");
		realtimeLB.setBounds(new Rectangle(100, 50, 200, 20));
		clockLB = new JLabel("Thread Clock : ");
		clockLB.setBounds(new Rectangle(10, 70, 90, 20));
		pclockLB = new JLabel("hh:mm:ss");
		pclockLB.setBounds(new Rectangle(100, 70, 200, 20));

		slider = new JSlider(model);
		slider.setBounds(new Rectangle(10, 92, 250, 20));
		pane = this.getContentPane();
		// this.setLayout(null);
		panel.add(clockLB);
		panel.add(pclockLB);
		panel.add(tpositionLB);
		panel.add(timeLB);
		panel.add(realtimeLB);
		panel.add(realtpositionLB);
		panel.add(lengthLB);
		panel.add(positionLB);
		panel.add(positionTF);
		panel.add(slider);
		panel.setBounds(new Rectangle(0, 0, 200, 20));
		pane.add(panel);
		this.setSize(300, 200);

		readPreferences();
	}

	public void setController(BasicController controller)
	{
	// this.controller = controller;
	}

	public String getName()
	{
		return "ProgressBar";
	}

	public void opened(Object stream, Map properties)
	{
		// If the plugin is not open, do nothing
		if (!isVisible())
			return;

		byteslength = -1;
		if (properties.containsKey("audio.length.bytes"))
		{
			byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
		}
		lengthLB.setText(" / " + byteslength);
		if (properties.containsKey("duration"))
		{
			milliseconds = (int) (((Long) properties.get("duration")).longValue()) / 1000;
		}
		else
		{
			// Try to compute duration
			int bitspersample = -1;
			int channels = -1;
			float samplerate = -1.0f;
			int framesize = -1;
			if (properties.containsKey("audio.samplesize.bits"))
			{
				bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue();
			}
			if (properties.containsKey("audio.channels"))
			{
				channels = ((Integer) properties.get("audio.channels")).intValue();
			}
			if (properties.containsKey("audio.samplerate.hz"))
			{
				samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue();
			}
			if (properties.containsKey("audio.framesize.bytes"))
			{
				framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue();
			}
			if (bitspersample > 0)
			{
				milliseconds = (int) (1000.0f * byteslength / (samplerate * channels * (bitspersample / 8)));
			}
			else
			{
				milliseconds = (int) (1000.0f * byteslength / (samplerate * framesize));
			}
		}
	}

	public void stateUpdated(BasicPlayerEvent event)
	{
		// If the plugin is not open, do nothing
		if (!isVisible())
			return;

		if (event.getCode() == BasicPlayerEvent.SEEKED)
		{
			float progress = event.getPosition() * 1.0f / byteslength * 1.0f;
			long progressMilliseconds = (long) (progress * milliseconds);
			timeLB.setText(millisec_to_time(progressMilliseconds) + " / "
			         + millisec_to_time(milliseconds) + " (" + (int) (100 * progress) + " %)");
			realtimeLB.setText(millisec_to_time(progressMilliseconds));
			model.setValue((int) (progress * modelscale));
			positionTF.setText(Long.toString(event.getPosition()));
			pclockLB.setText(millisec_to_time(clock));
		}
		else if (event.getCode() == BasicPlayerEvent.PLAYING)
		{
			double progress = event.getPosition() * 1.0f / this.byteslength * 1.0f;
			clock = (long) (progress * milliseconds);
			elapsed = clock;
			paused = false;
			synchronized (lock)
			{
				running = true;
			}
			Thread t = new Thread(this);
			t.start();
		}
		else if (event.getCode() == BasicPlayerEvent.PAUSED)
		{
			paused = true;
		}
		else if (event.getCode() == BasicPlayerEvent.RESUMED)
		{
			paused = false;
		}
		else if (event.getCode() == BasicPlayerEvent.STOPPED)
		{
			running = false;
		}
	}

	public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties)
	{
		// If the plugin is not open, do nothing
		if (!isVisible())
			return;

		long elapsedMilliseconds = microseconds / 1000;
		float progress = bytesread * 1.0f / this.byteslength * 1.0f;
		long progressMilliseconds = (long) (progress * milliseconds);
		timeLB.setText(millisec_to_time(progressMilliseconds) + " / "
		         + millisec_to_time(milliseconds) + " (" + (int) (100 * progress) + " %)");
		realtimeLB.setText(millisec_to_time(elapsed + elapsedMilliseconds));
		model.setValue((int) (progress * modelscale));
		positionTF.setText(Long.toString(bytesread));
		pclockLB.setText(millisec_to_time(clock));
	}

	private String millisec_to_time(long time_ms)
	{
		int seconds = (int) Math.floor(time_ms / 1000);
		int minutes = (int) Math.floor(seconds / 60);
		int hours = (int) Math.floor(minutes / 60);
		minutes = minutes - hours * 60;
		seconds = seconds - minutes * 60 - hours * 3600;
		String strhours = "" + hours;
		String strminutes = "" + minutes;
		String strseconds = "" + seconds;
		if (strseconds.length() == 1)
		{
			strseconds = "0" + strseconds;
		}
		if (strminutes.length() == 1)
		{
			strminutes = "0" + strminutes;
		}
		if (strhours.length() == 1)
		{
			strhours = "0" + strhours;
		}
		return (strhours + ":" + strminutes + ":" + strseconds);
	}

	public void run()
	{
		synchronized (lock)
		{
			System.out.println("Clock Thread started");
			while (running == true)
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				if (paused == false)
				{
					clock = clock + 1000;
				}
			}
			System.out.println("Clock Thread completed");
		}
	}

	/**
	 * getPlugin
	 * 
	 * @return playerPlugin
	 */
	public BasicPlayerListener getPlugin()
	{
		return this;
	}

	public String getVersion()
	{
		return "v1.0";
	}

}
