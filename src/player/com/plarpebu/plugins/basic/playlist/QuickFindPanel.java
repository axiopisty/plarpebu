package com.plarpebu.plugins.basic.playlist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.plarpebu.plugins.basic.SwingUtils;

public class QuickFindPanel extends JPanel implements ActionListener, KeyListener
{
	private JTextField quickFindField;

	private JButton quickFindCloseButton;

	private JButton quickFindNextButton;

	private JButton quickFindPrevButton;

	public QuickFindPanel(PlayListPlugin playListPlugin)
	{
		super();

		// Build Quick Find Panel
		addKeyListener(playListPlugin);
		
		quickFindCloseButton = SwingUtils.addButton(this, "Close QuickFind", "Close QuickFind",
		         "/icons/little/littleStop.gif");
		quickFindCloseButton.addActionListener(this);
		quickFindCloseButton.addKeyListener(playListPlugin);
		
		add(quickFindCloseButton);
		add(new JLabel("Find:"));
		quickFindField = new JTextField(20);
		quickFindField.addKeyListener(this);
		quickFindField.addKeyListener(playListPlugin);
		add(quickFindField);
		
		quickFindPrevButton = SwingUtils.addButton(this, "Find Prev", "Find Prev",
		         "/icons/little/littlePrev.gif");
		quickFindPrevButton.setMnemonic(KeyEvent.VK_P);
		quickFindPrevButton.addActionListener(this);
		quickFindPrevButton.addKeyListener(playListPlugin);
		add(quickFindPrevButton);
		
		quickFindNextButton = SwingUtils.addButton(this, "Find Next", "Find Next",
		         "/icons/little/littleNext.gif");
		quickFindNextButton.setMnemonic(KeyEvent.VK_N);
		quickFindNextButton.addActionListener(this);
		quickFindNextButton.addKeyListener(playListPlugin);
		add(quickFindNextButton);
	}

	/**
	 * Action performed
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == quickFindCloseButton)
		{
			this.setVisible(false);
		}
		else if (e.getSource() == quickFindNextButton)
		{
			findNext();
		}
		else if (e.getSource() == quickFindPrevButton)
		{
			findPrev();
		}
	}

	/**
	 * Invoked when a key has been pressed.
	 */
	public void keyPressed(KeyEvent e)
	{}

	/**
	 * Invoked when a key has been released.
	 */
	public void keyReleased(KeyEvent e)
	{}

	/**
	 * Invoked when a key has been typed.
	 */
	public void keyTyped(KeyEvent e)
	{
		// The textfield is not updated at this point, so we must kick off a thread.
		(new FindNextThread()).start();
	}

	/**
	 * When panel becomes visible, set focus to textfield
	 */
	@Override
	public void setVisible(boolean arg0)
	{
		super.setVisible(arg0);
		quickFindField.requestFocus();
		quickFindField.selectAll();
	}

	public void findNext()
	{
		// Search for the value of the textfield in the playlist
		String searchText = quickFindField.getText();
		System.out.println("Next searchText = " + searchText);
		if (searchText != null && searchText.equals("") == false)
			PlayListPlugin.findNext(searchText);
	}

	public void findPrev()
	{
		// Search for the value of the textfield in the playlist
		String searchText = quickFindField.getText();
		System.out.println("Prev searchText = " + searchText);
		if (searchText != null && searchText.equals("") == false)
			PlayListPlugin.findPrev(searchText);
	}

	private class FindNextThread extends Thread
	{
		public void run()
		{
			try
			{
				Thread.sleep(10);
				findNext();
			}
			catch (Exception ex)
			{
				System.out.println(ex);
			}
		}
	}
}
