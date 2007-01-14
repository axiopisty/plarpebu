package com.plarpebu.plugins.albumgrabber;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import com.bluedragontavern.albumgrabber.Album;
import com.bluedragontavern.albumgrabber.allmusic.AlbumSearch;
import com.plarpebu.plugins.sdk.FramePlugin;
import com.plarpebu.util.CacheUtil;

/**
 * Album grabber plugin
 * 
 * @author kmschmidt
 */
public class AlbumGrabberPlugin extends FramePlugin implements ActionListener, BasicPlayerListener
{

	private Album album = null;

	private RechercheThread st;

	private JLabel imageLabel = null;

	private JPopupMenu popup = null;

	private PopupListener popupListener = null;

	private SearchFrame searchframe = null;

	private String auteur = null;

	private String albumTitle = null;

	public AlbumGrabberPlugin()
	{
		super("Album Grabber");
		
		popupListener = new PopupListener();
		this.addMouseListener(popupListener);

		popup = new JPopupMenu();
		JMenuItem mi = new JMenuItem("Save Picture");
		mi.setActionCommand("save");
		mi.addActionListener(this);
		popup.add(mi);

		mi = new JMenuItem("Skip picture");
		mi.setActionCommand("skip");
		mi.addActionListener(this);
		popup.add(mi);

		popup.addSeparator();
		mi = new JMenuItem("Search +");
		mi.setActionCommand("search");
		mi.addActionListener(this);
		popup.add(mi);

		imageLabel = new JLabel();

		this.getContentPane().add(imageLabel);
		setSize(200, 200);
		readPreferences();
	}

	public String getName()
	{
		return "AlbumGrabber";
	}

	public String getAuteur()
	{
		return auteur;
	}

	public String getAlbum()
	{
		return albumTitle;
	}

	public void setAlbum(String auteurN, String albumN)
	{
		RechercheThread.yield();
		auteur = auteurN;
		albumTitle = albumN;
		try
		{
			File albumCacheDir = CacheUtil.getAlbumCacheDir();
			album = new Album(albumCacheDir, auteur, albumTitle);
			st = new RechercheThread(album, imageLabel, this);
			st.start();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void stateUpdated(BasicPlayerEvent event)
	{}

	public void opened(Object stream, Map properties)
	{
		if (isVisible())
		{
			if (stream != null)
			{
				auteur = ((String) properties.get("author"));
				albumTitle = ((String) properties.get("album"));
				try
				{
					File albumCacheDir = CacheUtil.getAlbumCacheDir();
					album = new Album(albumCacheDir, auteur, albumTitle);
					System.out.println("auteur : " + auteur + " album " + albumTitle);

					st = new RechercheThread(album, imageLabel, this);
					st.start();
				}
				catch (Exception ex)
				{}
			}
		}
	}

	public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties)
	{}

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
	public void setController(BasicController controller)
	{}

	/**
	 * actionPerformed
	 * 
	 * @param e
	 *           ActionEvent
	 */
	public void actionPerformed(ActionEvent e)
	{
		String c = e.getActionCommand();
		if (c.equals("search"))
		{
			searchframe = new SearchFrame(this);
			searchframe.setVisible(true);
		}
	}

	public String getVersion()
	{
		return "v1.2";
	}

	private class RechercheThread extends Thread
	{
		private Album album;

		private JLabel label;

		private JFrame frame;

		/**
		 * Constructor
		 * 
		 * @param album
		 *           DOCUMENT ME!
		 */
		public RechercheThread(Album aTrouver, JLabel label, JFrame frame)
		{
			this.album = aTrouver;
			this.label = label;
			this.frame = frame;
		}

		/**
		 * Run the search
		 */
		public void run()
		{
			try
			{
				album = AlbumSearch.search(album);
				label.setIcon(new ImageIcon(album.getImageBytes()));
				frame.pack();
			}
			catch (Exception ex)
			{
				System.out.println("Album non trouve");
			}
		}
	}

	/** Classe dédiée au Popup */
	class PopupListener extends MouseAdapter
	{

		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		/**
		 * Fonction qui initialise le PopUp lors de son affichage i.e. qui active ou désactive
		 * certains menus selon le contexte
		 */
		public void maybeShowPopup(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
				popup.revalidate();
			}
		}
	}
}
