package com.plarpebu;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import javazoom.jlgui.basicplayer.BasicPlayerListener;

import com.plarpebu.basicplayer.BasicPlayer;
import com.plarpebu.basicplayer.CompositePlayer;
import com.plarpebu.plugins.basic.info.InfoPlugin;
import com.plarpebu.plugins.basic.pan_gain.PanGainPlugin;
import com.plarpebu.plugins.basic.stop_play_seek.StopPlaySeekPlugin;
import com.plarpebu.plugins.sdk.FramePlugin;
import com.plarpebu.plugins.sdk.Iconifiable;
import com.plarpebu.plugins.sdk.JFrameWithPreferences;
import com.plarpebu.plugins.sdk.PanelPlugin;
import com.plarpebu.plugins.sdk.PlayerPlugin;
import com.plarpebu.util.ExitListenerSecurityManager;

import fr.unice.plugin.PluginManager;

/**
 * Main Player
 * 
 * @author Pepino, Arnaud
 * @version $Id
 */
public class Player extends JFrameWithPreferences implements DropTargetListener,
         MouseMotionListener, ComponentListener, Iconifiable
{
	/************************************************************************************************
	 * un tableau compose des noms des plugins que l'on veut afficher a l'origine et dont on souhaite
	 * leur affichage permanent
	 ***********************************************************************************************/
	public final static ArrayList<String> origine = new ArrayList<String>();

	static
	{
		// Set so that frame and dialogs are also decorated by SkinLF
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		origine.add(PanGainPlugin.NAME);
		origine.add(StopPlaySeekPlugin.NAME);
		origine.add(InfoPlugin.NAME);
	}

	// Splash Screen
	SplashScreen splashScreen = new SplashScreen();

	// New for R.Grin's lib
	private PluginManager pluginManager;

	private PlayerPlugin[] plugins;

	private PluginMenuItemFactory pluginMenuItemFactory;

	/** le ContentPane de l'interface principale */
	private Container pane;

	/** la JMenuBar de l'interface principale */
	private JMenuBar mb = new JMenuBar();

	private FramePlugin playlist;

	private ArrayList listComponent = new ArrayList();

	private BasicPlayer controller = null;

	private CompositePlayer bplayer = null;

	private JMenu menuPlugins = null;

	private static boolean playlistActivate = false;

	// Useful for detecting when program is exiting (so that plugins can save
	// themselves their preferences
	private ExitListenerSecurityManager sm;

	/**
	 * La fonction main : point d'entree du lecteur. Elle lance le Player.
	 */
	public static void main(String[] args) throws MalformedURLException
	{
		try
		{
			Player player = new Player();
			player.setVisible(true);
		}
		catch (Throwable ex)
		{
			System.out.println("Error in main: " + ex.toString());
			ex.printStackTrace();
		}
	}

	/**
	 * L'unique constructeur de Player. Il se charge de construire l'interface principale de notre
	 * lecteur.
	 * 
	 * @throws MalformedUrlException
	 */
	public Player() throws MalformedURLException
	{
		super("Plarpebu v1.0");

		// Use our own security manager, that can register listeners
		// All JFrameWithPreferences qare listeners, see contructor of this
		// class
		System.setSecurityManager(sm = new ExitListenerSecurityManager());
		sm.addSystemExitListener(this);

		splashScreen.showSplash();

		// NOW WE DO USE R.GRIN's lib for plugins management. Changes by M.Buffa

		// Do not pollute with too many messages. Comment this line for debug
		Logger.getLogger("fr.unice.plugin").setLevel(Level.ALL);

		// Specify the play.jar to load the basic plugins
		pluginManager = PluginManager.getPluginManager();
		pluginManager.addPluginManagerListener(splashScreen);

		// All jars in the plugins dir will be added automatically...
		pluginManager.addJarURLsInDirectories(new URL[]
		{ new URL("file:plugins") });
		pluginManager.loadPlugins();
		plugins = (PlayerPlugin[]) pluginManager.getPluginInstances(PlayerPlugin.class);

		pane = this.getContentPane();
		pane.setLayout(new GridLayout(0, 1));

		/* pour quitter l'application */
		preparationPourQuitter();

		/* on l'ajoute a different listener */
		addMouseMotionListener(this);
		addComponentListener(this);

		/* insertion de la JmenuBar dans la JFrame */
		setJMenuBar(mb);

		bplayer = new CompositePlayer();
		controller = bplayer;

		/* on creer nos menus ici */
		buildPluginMenu(); // le menu nous indiquant les fonctionnalites
		buildChargementMenu(); // simple menu pour recharcher d'eventuels
		// nouveau plugins
		buildSkinMenu(); // le menu pour changer de skins

		// Construction de l'interface a partir des plugins charg�s
		buildUI();

		/** Drag and drop */
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);

		// Remove splashScreen
		splashScreen.close();

		/* on charge automatiquement les plugins que l'on souhaite en permanence */
		activationOrigine();

		loadPreferences();
	}

	private void loadPreferences()
	{
		// Pr�f�rences, inherited from JFrameWithPreferences
		setPreferencesFileNames("preferences", "Plaperbu.properties", "defaultPlaperbu.properties");
		readPreferences();

		// Set the default skin
		SkinMgr.getInstance().setSkin(preferences.getProperty("lastSkin"));

		restoreVisibility();
	}

	/**
	 * Simple procedure preparant notre interface a l'operation de fermeture de la fenetre
	 * principale.<br>
	 * Affiche un une fenetre de dialogue pour confirmer le souhait de l'utilisateur a quitter
	 * l'application.<br>
	 * Ne provoque rien si l'utilisateur clique sur NON.<br>
	 * Provoque la fin de l'application impliquant la sauvegarde de la derniere configuration s'il
	 * clique sur OUI
	 */
	private void preparationPourQuitter()
	{
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				Object[] options =
				{ "YES", "NO" };
				int rep = JOptionPane.showOptionDialog(null, "Do you really want to quit ?", "Quit",
				         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				         options[0]);
				if (rep == JOptionPane.YES_OPTION)
				{
					/* l'utilisateur souhaite quitter le programme */
					System.exit(0);
				}
			}
		});
	}

	/**
	 * activationOrigine va permettre d'afficher des le lancement les plugins correspondant au
	 * tableau prive static origine. Elle met egalement a jour la liste des cases de plugins actives
	 * dans le menu. Notons que ceux-ci seront en affichage permanent puisque l'on bloque ensuite la
	 * possibilite de cacher le plugin. L'utilisateur desirant en rajouter d'autres en affichage
	 * permanent n'aurait simplement qu'a modifier dans le code le tableau origine en rajoutant la
	 * String correspondant au plugin . Par exemple en rajoutant origine.add("PlayList") dans le bloc
	 * static, la playliste deviendrait un plugin automatiquement lance a chaque session sans avoir
	 * la possibilite de decocher la case correspondante.
	 */
	private void activationOrigine()
	{
		// All the playerPlugins are in the plugins[] array

		// Pass the interface to the controller so that plugins will see it
		bplayer.setPlayerUI(this);

		JMenu men = pluginMenuItemFactory.getMenu();

		// So that plugins like kara cdg can iconify/de-iconify the player
		bplayer.setPlayerUI(this);

		// we need a handle on the playlist for possible hide/show from other
		// plugins
		if (playlist == null)
			playlist = (FramePlugin) searchPlugin("PlayList");

		// Fix by M.Buffa, Hop, we put all plugins as controler listeners
		for (int i = 0; i < plugins.length; i++)
		{
			bplayer.addBasicPlayerListener((BasicPlayerListener) plugins[i]);
			plugins[i].setController(bplayer);
			if (plugins[i] instanceof FramePlugin)
			{
				// All FramePlugins will listen to the SystemExit event... so
				// that they can save their preferences when we quit
				sm.addSystemExitListener((FramePlugin) plugins[i]);

				// Rstore visibility
				((FramePlugin) (plugins[i])).restoreVisibility();
			}
		}

		String namePlug;
		// affichage des plugins
		for (int i = 0; i < origine.size(); i++)
		{
			namePlug = (String) origine.get(i);
			PlayerPlugin pp = searchPlugin(namePlug);
			if (!(namePlug.equals(PanGainPlugin.NAME) || namePlug.equals(StopPlaySeekPlugin.NAME) || namePlug
			         .equals(InfoPlugin.NAME)))
			{
				// car buildUi se charge de l'agencement des ces plugins

				if (pp instanceof FramePlugin)
				{
					((FramePlugin) pp).setLocation(getX(), getY() + getHeight());
					((FramePlugin) pp).setVisible(true);
				}
				else if (pp instanceof PanelPlugin)
				{
					pane.add((Component) pp);
					pane.validate();
				}
			}

			((JCheckBoxMenuItem) men.getItem(getPluginIndex(pp))).setState(true);
			((JCheckBoxMenuItem) men.getItem(getPluginIndex(pp))).setEnabled(false);
		}
	}

	public int getPluginIndex(PlayerPlugin pp)
	{
		for (int i = 0; i < plugins.length; i++)
		{
			if (plugins[i] == pp)
				return i;
		}
		return -1;
	}

	/**
	 * La fonction searchPlugin est une fonction privee a Player. Elle va permettre de retrouver un
	 * playerPugin en fonction de son nom.
	 * 
	 * @param name
	 *           La String correspondant au nom du plugin.
	 * @return le playerPlugin correspondant.
	 */
	private PlayerPlugin searchPlugin(String name)
	{
		for (int i = 0; i < plugins.length; i++)
		{
			if (plugins[i].getName().equals(name))
				return plugins[i];
		}
		return null;
	}

	/**
	 * construit le menu liee aux skins si le repertoire skins est present et qu'il n'y a pas de pbs.
	 */
	private void buildSkinMenu()
	{
		try
		{
			ActionListener skinListener = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					SkinMgr.getInstance().setSkin(e.getActionCommand());
					preferences.setProperty("lastSkin", e.getActionCommand());
				}
			};

			JMenu menuSkins = new JMenu("Skins");

			List<String> skinNames = SkinMgr.getInstance().getSkinNames();
			for (String skinName : skinNames)
			{
				JMenuItem menuItem = new JMenuItem(skinName);
				menuItem.addActionListener(skinListener);
				menuSkins.add(menuItem);
			}

			mb.add(menuSkins);
		}
		catch (Exception ex)
		{
			System.out.println("Error building skins menu" + ex.toString());
		}
	}

	/**
	 * Construit les entrees du menu liees aux plugins.
	 */
	// CETTE METHODE EST A REVOIR !!! Mettre un thread qui scanne l'�tat des
	// plugins ?
	private void buildPluginMenu()
	{
		menuPlugins = new JMenu("Plugins");

		// L'actionListener qui va �couter les entr�es du menu des plugins
		ActionListener listener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
				String nameCommand = cb.getActionCommand();
				// System.out.print( (cb.getState() == true) ? "selectionne" :
				// "deseselectionne");
				System.out.println(" : " + nameCommand);

				PlayerPlugin pp = searchPlugin(nameCommand);

				if (cb.getState() == true)
				{
					bplayer.addBasicPlayerListener(pp.getPlugin());
					pp.setController(controller);
					// plugAffiche.add(pp);
					// System.out.println("Ajout ds plug
					// affiche"+plugAffiche.size());
					/*
					 * for(int j=0;j<plugAffiche.size();j++) ((Plugin)plugAffiche.get(j)).getName();
					 */
					if (pp instanceof FramePlugin)
					{
						// ( (FramePlugin) pp).setLocation(getX(), getY() +
						// getHeight());
						((FramePlugin) pp).setVisible(true);
					}
					else if (pp instanceof PanelPlugin)
					{
						pane.add((Component) pp);
						pane.validate();
					}
				}
				else
				{
					// plugAffiche.remove(pp);
					// System.out.println("Retrait ds plug affiche");
					if (pp instanceof FramePlugin)
					{
						((FramePlugin) pp).setVisible(false);
					}
					else if (pp instanceof PanelPlugin)
					{
						pane.remove((Component) pp);
						pane.validate();
					}
				}
			}
		};

		if (pluginMenuItemFactory == null)
		{
			pluginMenuItemFactory = new PluginMenuItemFactory(menuPlugins, pluginManager, listener);
		}
		buildPluginMenuEntries();
		// (panel1.popup).add(menuPlugins);

		mb.add(menuPlugins);

		// start a thread that updates the plugins states every 1/2 seconds
		startTimedPluginMenyUpdate();
	}

	private void startTimedPluginMenyUpdate()
	{
		javax.swing.Timer timer = new javax.swing.Timer(500, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				for (int i = 0; i < plugins.length; i++)
				{
					if (plugins[i] instanceof FramePlugin)
					{
						PluginMenuItem item = (PluginMenuItem) menuPlugins.getItem(i);
						// System.out.println("setting " + plugins[i].getName()
						// + " to " + ((FramePlugin) plugins[i]).isVisible());
						item.setState(((FramePlugin) plugins[i]).isVisible());
					}
				}
			}
		});
		timer.start();
	}

	private void buildPluginMenuEntries()
	{

		// Fait construire les entr�es du menu des plugins
		pluginMenuItemFactory.buildMenu(null);
	}

	/**
	 * Cette procedure construit le menu pour recharger les plugins.
	 */
	private void buildChargementMenu()
	{
		JMenu menuCharg = new JMenu("Reload");
		JMenuItem menuItem = new JMenuItem("Reload Plugins");
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				pluginManager.loadPlugins();
				plugins = (PlayerPlugin[]) pluginManager.getPluginInstances(PlayerPlugin.class);
				buildPluginMenuEntries();
				activationOrigine();
			}
		});
		menuCharg.add(menuItem);
		mb.add(menuCharg);
	}

	private void buildUI()
	{
		// Construction du lecteur a proprement parl�
		InfoPlugin listenerInfo = (InfoPlugin) searchPlugin(InfoPlugin.NAME);
		listenerInfo.setController(controller);
		bplayer.addBasicPlayerListener(listenerInfo.getPlugin());
		pane.add(listenerInfo);
		pane.validate();
		Thread t = new Thread(listenerInfo);
		t.start();

		Box panelUI = Box.createHorizontalBox();
		StopPlaySeekPlugin listenerSPS = (StopPlaySeekPlugin) searchPlugin(StopPlaySeekPlugin.NAME);
		listenerSPS.setController(bplayer);
		bplayer.addBasicPlayerListener(listenerSPS.getPlugin());

		PanGainPlugin listenerPG = (PanGainPlugin) searchPlugin(PanGainPlugin.NAME);
		listenerPG.setController(controller);
		bplayer.addBasicPlayerListener(listenerPG.getPlugin());

		panelUI.add(listenerSPS);
		panelUI.add(listenerPG);

		pane.add(panelUI);
	}

	public void displayComponent()
	{
		int taille = listComponent.size();
		pane.removeAll();
		for (int i = 0; i < taille; i++)
		{
			pane.add((JComponent) listComponent.get(i));
			((JComponent) listComponent.get(i)).revalidate();
			repaint();
		}
	}

	/**
	 * Fonction impl�ment�e pour l'interface mais non utile ici.
	 * 
	 * @param e
	 *           un evenement engendr� par la souris.
	 */
	public void mouseDragged(MouseEvent e)
	{}

	/**
	 * Fonction impl�ment�e pour l'interface mais non utile ici.
	 * 
	 * @param e
	 *           un evenement engendr� par la souris.
	 */
	public void mouseMoved(MouseEvent e)
	{}

	/**
	 * dragEnter
	 * 
	 * @param e
	 *           DropTargetDragEvent
	 */
	public void dragEnter(DropTargetDragEvent e)
	{
		if (isDragOk(e) == false)
		{
			e.rejectDrag();
			return;
		}
	}

	/**
	 * dragOver
	 * 
	 * @param e
	 *           DropTargetDragEvent
	 */
	public void dragOver(DropTargetDragEvent e)
	{
		if (isDragOk(e) == false)
		{
			e.rejectDrag();
			return;
		}
	}

	/**
	 * Fonction impl�ment�e pour l'interface mais non utile ici.
	 * 
	 * @param e
	 *           un DropTargetEvent.
	 */
	public void dragExit(DropTargetEvent e)
	{}

	/**
	 * dropActionChanged
	 * 
	 * @param e
	 *           DropTargetDragEvent
	 */
	public void dropActionChanged(DropTargetDragEvent e)
	{
		if (isDragOk(e) == false)
		{
			e.rejectDrag();
			return;
		}
	}

	/**
	 * ajoutes les fichiers � la playlist lors du drop nous pouvons ajouter un ou plusieurs fichier
	 * venant de l'exterieur ainsi que des playlist
	 * 
	 * @param e
	 *           DropTargetDropEvent
	 */
	public void drop(DropTargetDropEvent e)
	{
		// Verification DataFlavor
		DataFlavor[] dfs = e.getCurrentDataFlavors();
		DataFlavor tdf = null;
		for (int i = 0; i < dfs.length; i++)
		{
			if (DataFlavor.javaFileListFlavor.equals(dfs[i]) || DataFlavor.stringFlavor.equals(dfs[i]))
			{
				tdf = dfs[i];
				break;
			}
		}
		/** si on a trouve une bonne dataFlavor */
		if (tdf != null)
		{
			/** et que l'on souhite faire une copie */
			if ((e.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0)
			{
				/** le drop est accepte */
				e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			}
			else
			{
				return;
			}
			try
			{
				Transferable t = e.getTransferable();
				Object data = t.getTransferData(tdf);

				if (data instanceof java.util.List)
				{
					java.util.List al = (java.util.List) data;

					if (al.size() > 0)
					{
						File file = null;

						/**
						 * ajout des donn�es recues dans la liste graphique et r�elle
						 */
						ListIterator li = al.listIterator();
						while (li.hasNext())
						{
							file = (File) li.next();
							/** s'il s'agit de chansons */
							if (file != null && controller.isFileSupported(file))
							{
								// liste.add(file.getAbsolutePath());
								// reload();
								// System.out.println("on ajoute le fichier : "
								// + file.getName());
							}

						}
					}
				}
				else if (data instanceof java.lang.String)
				{
					System.out.println("on essaie d'ajouter ");
				}
			}
			catch (IOException ioe)
			{
				e.dropComplete(false);
				return;
			}
			catch (UnsupportedFlavorException ufe)
			{
				e.dropComplete(false);
				return;
			}
			catch (Exception ex)
			{
				e.dropComplete(false);
				return;
			}
			e.dropComplete(true);
		}
	}

	/**
	 * isDragOk : v�rifie si le DnD est possible il est possible si on essaie de "droper" soit un
	 * fichier soit une String
	 * 
	 * @param e
	 *           DropTargetDragEvent
	 * @return boolean
	 */
	private boolean isDragOk(DropTargetDragEvent e)
	{
		// verification DataFlavor
		DataFlavor[] dfs = e.getCurrentDataFlavors();
		DataFlavor tdf = null;
		for (int i = 0; i < dfs.length; i++)
		{
			/**
			 * si on essaie d'ajouter un fichier ou du texte, cad un path pour un fichier mp3
			 */
			if (DataFlavor.javaFileListFlavor.equals(dfs[i]) || DataFlavor.stringFlavor.equals(dfs[i]))
			{
				tdf = dfs[i];
				break;
			}
		}
		if (tdf != null)
		{
			if ((e.getSourceActions() & DnDConstants.ACTION_COPY_OR_MOVE) != 0)
			{
				return true;
			}

			return false;
		}

		return false;
	}

	/**
	 * Fonction impl�ment�e pour l'interface mais non utile ici.
	 * 
	 * @param e
	 *           un ComponentEvent.
	 */
	public void componentHidden(ComponentEvent e)
	{}

	/**
	 * Fonction impl�ment�e pour l'interface mais non utile ici.
	 * 
	 * @param e
	 *           un ComponentEvent.
	 */
	public void componentMoved(ComponentEvent e)
	{}

	/**
	 * componentResized
	 * 
	 * @param e
	 *           ComponentEvent
	 */
	public void componentResized(ComponentEvent e)
	{}

	/**
	 * Fonction impl�ment�e pour l'interface mais non utile ici.
	 * 
	 * @param e
	 *           un ComponentEvent.
	 */
	public void componentShown(ComponentEvent e)
	{}

	public static boolean isActivate()
	{
		return playlistActivate;
	}

	public void minimize()
	{
		System.out.println("---hide()---");
		setVisible(false);

		// For the playlist plugin also (progess bar is seen through the kar cdg
		// windows)
		if (playlist != null)
		{
			System.out.println("We hide the playlist");

			playlist.setVisible(false);
		}
	}

	public void setToOriginalSize()
	{
		System.out.println("---show()---");
		setVisible(true);
		// For the playlist plugin also (progess bar is seen through the kar cdg
		// windows)
		if (playlist != null)
			playlist.setVisible(true);
	}
}
