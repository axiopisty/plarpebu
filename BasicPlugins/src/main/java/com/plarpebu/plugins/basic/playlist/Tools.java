package com.plarpebu.plugins.basic.playlist;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.plarpebu.SkinMgr;
import com.plarpebu.javakarplayer.plugins.AudioPlugins.taras.FontDialogChoser;
import com.plarpebu.util.UserInterfaceUtil;

/**
 * Playlist Configuration UI
 */
public class Tools extends JFrame implements ActionListener, ItemListener, PropertyChangeListener
{
	private Container pane = null;

	private JPanel fontPanel;

	private JPanel cbPanel;

	private JPanel itemPanel;

	private JPanel itemPanel2;

	private JLabel fgILabel;

	private JLabel bgILabel;

	private JLabel fgSLabel;

	private JLabel bgSLabel;

	private JCheckBox cb1;

	private JCheckBox cb2;

	private JCheckBox cb3;

	private JCheckBox cb4;

	private ColorControl fgI = null;

	private ColorControl bgI = null;

	private ColorControl fgS = null;

	private ColorControl bgS = null;

	private boolean change = false;

	private PlayListPlugin playlist = null;

	private FontDialogChoser fc = new FontDialogChoser();

	private JButton chooseFontButton;

	/**
	 * Constructor
	 * 
	 * @param pl
	 */
	public Tools(PlayListPlugin pl)
	{
		super("Playlist Configuration");
		playlist = pl;
		try
		{
			jbInit();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		SkinMgr.getInstance().addComponent(this);
	}

	/**
	 * Initialize
	 * 
	 * @throws Exception
	 */
	void jbInit() throws Exception
	{

		pane = getContentPane();
		pane.setLayout(new GridLayout(4, 0));

		fontPanel = new JPanel(new GridLayout(3, 2));
		fontPanel.setBorder(BorderFactory.createTitledBorder("Font"));

		chooseFontButton = new JButton("Choose Font");
		chooseFontButton.addActionListener(this);

		fontPanel.add(chooseFontButton);

		cb1 = new JCheckBox("Include SubFolders for Drag and Drop", true);
		cb1.addItemListener(this);
		cb1.setSelected(playlist.isIncludeSubFolderForDragAndDrop());

		cb2 = new JCheckBox("Show Player Buttons in StatusBar", true);
		cb2.addItemListener(this);
		cb2.setSelected(playlist.isShowPlayerButtonsInStatusBar());

		cb3 = new JCheckBox("Single Song Mode", false);
		cb3.addItemListener(this);
		cb3.setSelected(playlist.isSingleSongMode());

		cb4 = new JCheckBox("Show Line Numbers in PlayList", false);
		cb4.addItemListener(this);
		cb4.setSelected(playlist.isShowLineNumbersInPlayList());

		cbPanel = new JPanel(new GridLayout(4, 0));
		cbPanel.add(cb1);
		cbPanel.add(cb2);
		cbPanel.add(cb3);
		cbPanel.add(cb4);

		itemPanel = new JPanel(new GridLayout(2, 2));
		itemPanel.setBorder(BorderFactory.createTitledBorder("All Items Style"));

		fgILabel = new JLabel("  Text Color : ");
		fgI = new ColorControl(this);
		fgI.addPropertyChangeListener(this);

		bgILabel = new JLabel("  Background Color : ");
		bgI = new ColorControl(this);
		bgI.addPropertyChangeListener(this);

		itemPanel.add(fgILabel);
		itemPanel.add(fgI);
		itemPanel.add(bgILabel);
		itemPanel.add(bgI);

		itemPanel2 = new JPanel(new GridLayout(2, 2));
		itemPanel2.setBorder(BorderFactory.createTitledBorder("Selected Item Style"));

		fgSLabel = new JLabel("  Text Color : ");
		fgS = new ColorControl(this);
		fgS.setPreferredSize(new Dimension(30, 30));
		fgS.addPropertyChangeListener(this);

		bgSLabel = new JLabel("  Background Color: ");
		bgS = new ColorControl(this);
		bgS.setPreferredSize(new Dimension(30, 30));
		bgS.addPropertyChangeListener(this);

		itemPanel2.add(fgSLabel);
		itemPanel2.add(fgS);
		itemPanel2.add(bgSLabel);
		itemPanel2.add(bgS);

		pane.setLayout(new GridLayout(4, 0));
		pane.add(fontPanel);
		pane.add(cbPanel);
		pane.add(itemPanel);
		pane.add(itemPanel2);
		this.setSize(300, 400);
		UserInterfaceUtil.centerFrame(this);
	}

	/**
	 * ActionListener callback
	 */
	public void actionPerformed(ActionEvent e)
	{
		// Called by the "choose font" button
		fc.setSelectedFont(playlist.getPlaylistFont());
		fc.setVisible(true);
		Font f = fc.getSelectedFont();
		if (f != null)
		{
			playlist.setPlayListFont(f);
		}
	}

	public void setChange(boolean b)
	{
		change = b;
	}

	/**
	 * itemStateChanged
	 * 
	 * @param e
	 *           ItemEvent
	 */
	public void itemStateChanged(ItemEvent e)
	{
		Object list = e.getSource();

		// Include SubFolders for Drag and Drop
		if (list == cb1)
		{
			playlist.setIncludeSubFolderForDragAndDrop(cb1.isSelected());
		}
		// Show Player Buttons in StatusBar
		else if (list == cb2)
		{
			playlist.setShowPlayerButtonsInStatusBar(cb2.isSelected());
		}
		// Single Song Mode changed, update playlist prefs
		else if (list == cb3)
		{
			playlist.setSingleSongMode(cb3.isSelected());
		}
		// Show Line Numbers in PlayList
		else if (list == cb4)
		{
			playlist.setShowLineNumbersInPlayList(cb4.isSelected());
		}
	}

	/**
	 * propertyChange
	 * 
	 * @param evt
	 *           PropertyChangeEvent
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (change)
		{
			if (evt.getSource() == fgI)
				playlist.setPlayListFg(fgI.getBackground());
			else if (evt.getSource() == bgI)
				playlist.setPlayListBg(bgI.getBackground());
			else if (evt.getSource() == fgS)
			{
				playlist.setPlayListSelectionFg(fgS.getBackground());
			}
			else if (evt.getSource() == bgS)
				playlist.setPlayListSelectionBg(bgS.getBackground());
		}
	}

	public void setBgColor(Color bgColor)
	{
		bgI.setBackground(bgColor);
	}

	public void setFgColor(Color fgColor)
	{
		fgI.setBackground(fgColor);
	}

	public void setSelectionFgColor(Color selectionFgColor)
	{
		fgS.setBackground(selectionFgColor);
	}

	public void setSelectionBgColor(Color selectionBgColor)
	{
		bgS.setBackground(selectionBgColor);
	}
}
