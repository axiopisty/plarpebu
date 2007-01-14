package com.plarpebu.javakarplayer.plugins.AudioPlugins.taras;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MediaTracker;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class PreferencesDialog extends JDialog implements PropertyChangeListener
{
	Border border1;

	TitledBorder titledBorder1;

	Border border2;

	Border border3;

	Border border4;

	Border border5;

	TitledBorder titledBorder2;

	ButtonGroup buttonGroup2 = new ButtonGroup();

	Border border6;

	TitledBorder titledBorder3;

	JFileChooser chooser;

	private KaraokePane karaokePane;

	private FontDialogChoser fc = new FontDialogChoser();

	private boolean charsetAutodetection;

	private String charset;

	private boolean notifyKaraokePane = false;

	Border border7;

	TitledBorder titledBorder4;

	SpinnerNumberModel modelNbrows = new SpinnerNumberModel(6, 2, 20, 1);

	SpinnerNumberModel modelNbColumns = new SpinnerNumberModel(40, 20, 70, 1);

	SpinnerNumberModel modelNbReadLines = new SpinnerNumberModel(1, 0, 5, 1);

	SpinnerNumberModel modelOutlineWidth = new SpinnerNumberModel(4, 1, 20, 1);

	JTabbedPane jTabbedPane1 = new JTabbedPane();

	JPanel jPanelLyrics = new JPanel();

	JRadioButton jRadioButtonImage = new JRadioButton();

	JComboBox jComboBoxRadiantType = new JComboBox();

	JPanelColorSelector colorSelectorGradiantEnd = new JPanelColorSelector();

	JButton jButtonBrowseImage = new JButton();

	JRadioButton jRadioButtonMonochrome = new JRadioButton();

	JRadioButton jRadioButtonGradiant = new JRadioButton();

	JPanelColorSelector colorSelectorGradiantStart = new JPanelColorSelector();

	XYLayout xYLayout3 = new XYLayout();

	JPanelColorSelector colorSelectorMonochrome = new JPanelColorSelector();

	JLabel jLabel11 = new JLabel();

	JPanel jPanelBackground = new JPanel();

	JTextField jTextFieldImageFilename = new JTextField();

	JLabel jLabel10 = new JLabel();

	XYLayout xYLayout6 = new XYLayout();

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel jPanelSouth = new JPanel();

	JButton jButtonClose = new JButton();

	JCheckBox jCheckBoxAntialiasing = new JCheckBox();

	JLabel jLabel1 = new JLabel();

	JLabel jLabel3 = new JLabel();

	JCheckBox jCheckBoxOutline = new JCheckBox();

	JCheckBox jCheckBoxShadow = new JCheckBox();

	JPanelColorSelector colorSelectorActiveSyllabe = new JPanelColorSelector();

	JLabel jLabel2 = new JLabel();

	JLabel jLabel14 = new JLabel();

	JPanel jPanelLyricsDisplay = new JPanel();

	JPanelColorSelector colorSelectorSungSyllabe = new JPanelColorSelector();

	JLabel jLabel9 = new JLabel();

	JSpinner jSpinnerOutlineWidth = new JSpinner();

	JSlider jSliderShadowOpacity = new JSlider();

	JPanelColorSelector colorSelectorShadow = new JPanelColorSelector();

	JLabel jLabel7 = new JLabel();

	JPanelColorSelector colorSelectorOutline = new JPanelColorSelector();

	JPanelColorSelector colorSelectorToSingSyllabe = new JPanelColorSelector();

	XYLayout xYLayout2 = new XYLayout();

	JLabel jLabel15 = new JLabel();

	JLabel jLabel4 = new JLabel();

	JSpinner jSpinnerNbLinesOnTop = new JSpinner();

	JLabel jLabel8 = new JLabel();

	JLabel jLabel12 = new JLabel();

	JPanel jPanel4 = new JPanel();

	XYLayout xYLayout4 = new XYLayout();

	JSpinner jSpinnerNbColumns = new JSpinner();

	JSpinner jSpinnerNbRows = new JSpinner();

	XYLayout xYLayout5 = new XYLayout();

	JPanel jPanelLyricsLayout = new JPanel();

	JLabel jLabel6 = new JLabel();

	JLabel jLabel5 = new JLabel();

	JPanel jPanel1 = new JPanel();

	Border border8;

	TitledBorder titledBorder5;

	XYLayout xYLayout1 = new XYLayout();

	JLabel jLabel13 = new JLabel();

	JButton jButtonChooseFont = new JButton();

	JComboBox jComboBoxCharsets = new JComboBox();

	JLabel jLabelManualCharset = new JLabel();

	JComboBox jComboBoxCharsetHints = new JComboBox();

	JCheckBox jCheckBoxCharsetAutodetect = new JCheckBox();

	JLabel jLabel16 = new JLabel();

	JLabel jLabel17 = new JLabel();

	JPanel jPanel5 = new JPanel();

	XYLayout xYLayout7 = new XYLayout();

	public PreferencesDialog()
	{
		try
		{
			fillCharsetComboBoxWithAvailableCharsets();
			fillCharsetHintsComboBox();

			jbInit();

			// In order to do something when the component is shown
			addComponentListener(new MyComponentAdapter());

			// setSize(420, 600);
			pack();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public class MyComponentAdapter extends ComponentAdapter
	{
		public void componentShown(ComponentEvent e)
		{
			// Listen to property change only once the component is visible ?
			enableNotifyPropertyChanges();
		}
	}

	private void installPropertyChangeListeners()
	{
		colorSelectorSungSyllabe.addPropertyChangeListener(this);
		colorSelectorActiveSyllabe.addPropertyChangeListener(this);
		colorSelectorToSingSyllabe.addPropertyChangeListener(this);
		colorSelectorShadow.addPropertyChangeListener(this);
		colorSelectorOutline.addPropertyChangeListener(this);
		colorSelectorGradiantStart.addPropertyChangeListener(this);
		colorSelectorGradiantEnd.addPropertyChangeListener(this);
		colorSelectorMonochrome.addPropertyChangeListener(this);
	}

	private void fillCharsetComboBoxWithAvailableCharsets()
	{
		// Add list of available charsets to the combo box
		Map map = Charset.availableCharsets();
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			// Get charset namechars
			String charsetName = (String) it.next();

			// Get charset
			Charset charset = Charset.forName(charsetName);
			jComboBoxCharsets.addItem(charset);
		}
	}

	private void fillCharsetHintsComboBox()
	{
		jComboBoxCharsetHints.addItem("Don't know");
		jComboBoxCharsetHints.addItem("Japanese");
		jComboBoxCharsetHints.addItem("Chinese");
		jComboBoxCharsetHints.addItem("Simplified chinese");
		jComboBoxCharsetHints.addItem("Traditional chinese");
		jComboBoxCharsetHints.addItem("Korean");
	}

	public void setKaraokePane(KaraokePane karaokePane)
	{
		if (this.karaokePane == null)
		{
			this.karaokePane = karaokePane;

			// Set up initial values
			colorSelectorSungSyllabe.setBackground(karaokePane.getActiveColor());
			colorSelectorActiveSyllabe.setBackground(karaokePane.getHlColor());
			colorSelectorToSingSyllabe.setBackground(karaokePane.getFgColor());
			colorSelectorShadow.setBackground(karaokePane.getShadowColor());
			colorSelectorGradiantStart.setBackground(karaokePane.getGradiantStartColor());
			colorSelectorGradiantEnd.setBackground(karaokePane.getGradiantEndColor());
			colorSelectorOutline.setBackground(karaokePane.getOutlineColor());
			colorSelectorMonochrome.setBackground(karaokePane.getMonochromeColor());

			init(karaokePane.props);
			installPropertyChangeListeners();
		}
	}

	public void init(KaraokeProperties props)
	{
		// Outlined text
		jCheckBoxOutline.setSelected(props.displayOutline);
		jSpinnerOutlineWidth.setValue(new Integer(props.outlineWidth));

		// Anti-aliasing
		jCheckBoxAntialiasing.setSelected(props.antiAliasedText);

		// Shadowed text
		jCheckBoxShadow.setSelected(props.displayShadow);
		colorSelectorShadow.setEnabled(jCheckBoxShadow.isSelected());
		jSliderShadowOpacity.setEnabled(jCheckBoxShadow.isSelected());

		// select current charset in the combobox
		int nbItems = jComboBoxCharsets.getItemCount();
		for (int i = 0; i < nbItems; i++)
		{
			if (jComboBoxCharsets.getItemAt(i).toString().equals(props.charset))
			{
				jComboBoxCharsets.setSelectedIndex(i);
				break;
			}
		}
		jComboBoxCharsets.setEnabled(!jCheckBoxCharsetAutodetect.isSelected());

		// charsets autodetection ?
		jCheckBoxCharsetAutodetect.setSelected(props.autodetectCharset);
		jComboBoxCharsetHints.setEnabled(jCheckBoxCharsetAutodetect.isSelected());
		jComboBoxCharsetHints.setSelectedIndex(props.charsetHint);
		jLabelManualCharset.setEnabled(!jCheckBoxCharsetAutodetect.isSelected());

		// Text layouts
		jSpinnerNbColumns.setValue(new Integer(props.cols));
		jSpinnerNbRows.setValue(new Integer(props.lines));
		jSpinnerNbLinesOnTop.setValue(new Integer(props.readLine));

		// Background mode
		if (props.backgroundType == KaraokeProperties.BACKGROUND_GRADIANT)
		{
			jRadioButtonGradiant.setSelected(true);
		}
		else if (props.backgroundType == KaraokeProperties.BACKGROUND_PLAIN)
		{
			jRadioButtonMonochrome.setSelected(true);
		}
		else if (props.backgroundType == KaraokeProperties.BACKGROUND_IMAGE)
		{
			jRadioButtonImage.setSelected(true);
		}
		// we set this even if the background mode is not image
		jTextFieldImageFilename.setText(props.bgImageFilename);
	}

	public void enableNotifyPropertyChanges()
	{

		notifyKaraokePane = true;
	}

	/**
	 * propertyChange
	 * 
	 * @param evt
	 *           PropertyChangeEvent
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{

		if (!notifyKaraokePane)
			return;

		if (evt.getSource() == colorSelectorSungSyllabe)
		{
			karaokePane.setActiveColor(colorSelectorSungSyllabe.getBackground());
		}
		else if (evt.getSource() == colorSelectorActiveSyllabe)
		{
			karaokePane.setHlColor(colorSelectorActiveSyllabe.getBackground());
		}
		else if (evt.getSource() == colorSelectorToSingSyllabe)
		{
			karaokePane.setFgColor(colorSelectorToSingSyllabe.getBackground());
		}
		else if (evt.getSource() == colorSelectorShadow)
		{
			karaokePane.setShadowColor(colorSelectorShadow.getBackground());
		}
		else if (evt.getSource() == colorSelectorOutline)
		{
			karaokePane.setOutlineColor(colorSelectorOutline.getBackground());
		}
		else if (evt.getSource() == colorSelectorGradiantStart)
		{
			karaokePane.setGradiantStartColor(colorSelectorGradiantStart.getBackground());
		}
		else if (evt.getSource() == colorSelectorGradiantEnd)
		{
			karaokePane.setGradiantEndColor(colorSelectorGradiantEnd.getBackground());
		}
		else if (evt.getSource() == colorSelectorMonochrome)
		{
			karaokePane.setMonochromeColor(colorSelectorMonochrome.getBackground());
		}
	}

	// The window will appear centered
	public void setVisible(boolean flag)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}
		setLocation((screenSize.width - frameSize.width) / 2,
		         (screenSize.height - frameSize.height) / 2);

		super.setVisible(flag);

	}

	void jbInit() throws Exception
	{
		border1 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 1),
		         "Lyrics display");
		border2 = BorderFactory.createLineBorder(Color.black, 2);
		border3 = BorderFactory.createLineBorder(Color.black, 2);
		border4 = BorderFactory.createLineBorder(Color.black, 2);
		border5 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		titledBorder2 = new TitledBorder(border5, "Background");
		border6 = BorderFactory.createEmptyBorder();
		titledBorder3 = new TitledBorder(BorderFactory.createLineBorder(Color.black, 1),
		         "Lyrics preview");
		border7 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		titledBorder4 = new TitledBorder(border7, "Lyrics layout");
		border8 = BorderFactory.createLineBorder(Color.black, 2);
		titledBorder5 = new TitledBorder(BorderFactory.createLineBorder(Color.black, 1), "Charset");
		this.getContentPane().setLayout(borderLayout1);
		this.setModal(true);
		this.setTitle("Preferences");
		jRadioButtonImage.setText("Background picture");
		jRadioButtonImage.addItemListener(new PreferencesDialog_jRadioButtonImage_itemAdapter(this));
		colorSelectorGradiantEnd.setBackground(UIManager.getColor("activeCaption"));
		jButtonBrowseImage.setText("Browse");
		jButtonBrowseImage.addActionListener(new PreferencesDialog_jButtonBrowseImage_actionAdapter(
		         this));
		jButtonBrowseImage.setEnabled(false);
		jRadioButtonMonochrome.setText("Monochrome");
		jRadioButtonMonochrome
		         .addItemListener(new PreferencesDialog_jRadioButtonMonochrome_itemAdapter(this));
		jRadioButtonGradiant.setText("Gradiant");
		jRadioButtonGradiant.addItemListener(new PreferencesDialog_jRadioButtonGradiant_itemAdapter(
		         this));
		jRadioButtonGradiant.setSelected(true);
		colorSelectorGradiantStart.setBackground(SystemColor.inactiveCaption);
		colorSelectorMonochrome.setBackground(UIManager.getColor("Tree.selectionBackground"));
		jLabel11.setText("End color");
		jPanelBackground.setLayout(xYLayout3);
		jPanelBackground.setBorder(titledBorder2);
		jTextFieldImageFilename.setText("No picture");
		jTextFieldImageFilename.setEnabled(false);
		jLabel10.setText("Start color");
		jPanelLyrics.setLayout(xYLayout6);
		jButtonClose.setText("Close");
		jButtonClose.addActionListener(new PreferencesDialog_jButtonClose_actionAdapter(this));
		jCheckBoxAntialiasing.setSelected(true);
		jCheckBoxAntialiasing.setText("Antialiasing of text");
		jLabel1.setBackground(Color.green);
		jLabel1.setText("Active syllabe");
		jLabel3.setMaximumSize(new Dimension(62, 15));
		jLabel3.setText("Sung syllabe");
		jCheckBoxOutline.setSelected(true);
		jCheckBoxOutline.setText("Outline");
		jCheckBoxOutline.addItemListener(new PreferencesDialog_jCheckBoxOutline_itemAdapter(this));

		jCheckBoxShadow.setSelected(true);
		jCheckBoxShadow.setText("Shadow");
		jCheckBoxShadow.addItemListener(new PreferencesDialog_jCheckBoxShadow_itemAdapter(this));

		colorSelectorActiveSyllabe.setBackground(Color.green);
		jLabel2.setBackground(Color.yellow);
		jLabel2.setText("To sing syllabe");
		jLabel14.setText("Color");
		jPanelLyricsDisplay.setBorder(titledBorder1);
		jPanelLyricsDisplay.setLayout(xYLayout2);
		colorSelectorSungSyllabe.setBackground(Color.red);
		jLabel9.setText("Opacity");
		jSpinnerOutlineWidth.setModel(modelOutlineWidth);
		jSpinnerOutlineWidth
		         .addChangeListener(new PreferencesDialog_jSpinnerOutlineWidth_changeAdapter(this));
		jSliderShadowOpacity.setMaximum(100);
		jSliderShadowOpacity.setMinimum(0);
		jSliderShadowOpacity.setMinorTickSpacing(0);
		jSliderShadowOpacity.setPaintLabels(false);
		jSliderShadowOpacity.setPaintTicks(false);
		jSliderShadowOpacity.setToolTipText("Opacity value");
		colorSelectorShadow.setBackground(SystemColor.windowBorder);
		jLabel7.setText("Color");
		colorSelectorOutline.setBackground(SystemColor.windowBorder);
		colorSelectorToSingSyllabe.setBackground(Color.yellow);
		jLabel15.setText("Size");
		jLabel4.setForeground(Color.red);
		jLabel4.setText("These values will be taken into account");
		jSpinnerNbLinesOnTop.setModel(modelNbReadLines);
		jSpinnerNbLinesOnTop
		         .addChangeListener(new PreferencesDialog_jSpinnerNbLinesOnTop_changeAdapter(this));

		jLabel8.setText("Nb read lines on top");
		jLabel12.setForeground(Color.red);
		jLabel12.setText("when you play the next song !");
		jPanel4.setBorder(BorderFactory.createLineBorder(Color.black));
		jPanel4.setLayout(xYLayout5);
		jSpinnerNbColumns.setModel(modelNbColumns);
		jSpinnerNbColumns.setToolTipText("");
		jSpinnerNbColumns.addChangeListener(new PreferencesDialog_jSpinnerNbColumns_changeAdapter(
		         this));

		jSpinnerNbRows.setModel(modelNbrows);
		jSpinnerNbRows.addChangeListener(new PreferencesDialog_jSpinnerNbRows_changeAdapter(this));

		jPanelLyricsLayout.setBorder(titledBorder4);
		jPanelLyricsLayout.setLayout(xYLayout4);
		jLabel6.setText("Nb rows");
		jLabel5.setText("Nb columns");
		jPanel1.setBorder(titledBorder5);
		jPanel1.setLayout(xYLayout1);
		jLabel13.setText("Hints");
		jButtonChooseFont.setText("Choose font");
		jButtonChooseFont.addActionListener(new PreferencesDialog_jButtonChooseFont_actionAdapter(
		         this));
		jComboBoxCharsets.setEnabled(false);
		jComboBoxCharsets.addActionListener(new PreferencesDialog_jComboBoxCharsets_actionAdapter(
		         this));

		jLabelManualCharset.setEnabled(false);
		jLabelManualCharset.setText("Choose charset");
		jCheckBoxCharsetAutodetect.setSelected(true);
		jCheckBoxCharsetAutodetect.setText("Charset autodetect");
		jCheckBoxCharsetAutodetect
		         .addItemListener(new PreferencesDialog_jCheckBoxCharsetAutodetect_itemAdapter(this));

		jLabel16.setText("These values will be taken into account");
		jLabel16.setForeground(Color.red);
		jLabel17.setText("when you play the next song !");
		jLabel17.setForeground(Color.red);
		jPanel5.setLayout(xYLayout7);
		jPanel5.setBorder(BorderFactory.createLineBorder(Color.black));
		jComboBoxCharsetHints
		         .addActionListener(new PreferencesDialog_jComboBoxCharsetHints_actionAdapter(this));
		this.getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
		jTabbedPane1.add(jPanelLyrics, "Lyrics");
		jTabbedPane1.add(jPanelBackground, "Background");
		jPanelBackground.add(jRadioButtonGradiant, new XYConstraints(6, 5, -1, -1));
		jPanelBackground.add(jComboBoxRadiantType, new XYConstraints(80, 8, 122, 19));
		jPanelBackground.add(jRadioButtonMonochrome, new XYConstraints(6, 97, -1, -1));
		jPanelBackground.add(jRadioButtonImage, new XYConstraints(6, 138, 126, 22));
		jPanelBackground.add(colorSelectorMonochrome, new XYConstraints(127, 101, 18, 15));
		jPanelBackground.add(jLabel10, new XYConstraints(28, 40, -1, -1));
		jPanelBackground.add(jLabel11, new XYConstraints(28, 64, 50, -1));
		jPanelBackground.add(colorSelectorGradiantStart, new XYConstraints(127, 40, 18, 15));
		jPanelBackground.add(colorSelectorGradiantEnd, new XYConstraints(127, 64, 18, 15));
		jPanelBackground.add(jTextFieldImageFilename, new XYConstraints(125, 170, 112, 20));
		jPanelBackground.add(jButtonBrowseImage, new XYConstraints(28, 170, 80, 20));
		this.getContentPane().add(jPanelSouth, BorderLayout.SOUTH);
		jPanelSouth.add(jButtonClose, null);
		jPanelLyricsDisplay.add(jButtonChooseFont, new XYConstraints(11, 11, -1, 19));
		jPanelLyricsDisplay.add(jLabel3, new XYConstraints(11, 53, -1, -1));
		jPanelLyricsDisplay.add(jLabel1, new XYConstraints(11, 75, -1, -1));
		jPanelLyricsDisplay.add(jLabel2, new XYConstraints(11, 97, -1, -1));
		jPanelLyricsDisplay.add(colorSelectorToSingSyllabe, new XYConstraints(125, 97, 18, 15));
		jPanelLyricsDisplay.add(colorSelectorActiveSyllabe, new XYConstraints(125, 75, 18, 15));
		jPanelLyricsDisplay.add(colorSelectorSungSyllabe, new XYConstraints(125, 53, 18, 15));
		jPanelLyricsDisplay.add(jCheckBoxShadow, new XYConstraints(11, 130, 113, 19));
		jPanelLyricsDisplay.add(jLabel7, new XYConstraints(33, 162, 59, -1));
		jPanelLyricsDisplay.add(jLabel9, new XYConstraints(33, 190, -1, 18));
		jPanelLyricsDisplay.add(jSliderShadowOpacity, new XYConstraints(98, 192, 70, 15));
		jPanelLyricsDisplay.add(colorSelectorShadow, new XYConstraints(124, 162, 18, 15));
		jPanelLyricsDisplay.add(jCheckBoxOutline, new XYConstraints(11, 222, 68, 21));
		jPanelLyricsDisplay.add(jSpinnerOutlineWidth, new XYConstraints(122, 278, 46, 21));
		jPanelLyricsDisplay.add(colorSelectorOutline, new XYConstraints(124, 254, 18, 15));
		jPanelLyricsDisplay.add(jCheckBoxAntialiasing, new XYConstraints(11, 305, -1, -1));
		jPanelLyricsDisplay.add(jLabel15, new XYConstraints(33, 279, 39, 18));
		jPanelLyricsDisplay.add(jLabel14, new XYConstraints(32, 254, 68, -1));
		jPanelLyrics.add(jPanel1, new XYConstraints(241, 206, 253, 178));
		jPanelLyrics.add(jPanelLyricsLayout, new XYConstraints(241, 20, 253, 178));
		jPanelLyricsLayout.add(jLabel5, new XYConstraints(9, 7, -1, -1));
		jPanelLyricsLayout.add(jLabel6, new XYConstraints(9, 34, 56, 21));
		jPanelLyricsLayout.add(jLabel8, new XYConstraints(9, 67, 107, 17));
		jPanelLyricsLayout.add(jSpinnerNbColumns, new XYConstraints(135, 5, 56, 19));
		jPanelLyricsLayout.add(jSpinnerNbRows, new XYConstraints(135, 35, 56, 19));
		jPanelLyricsLayout.add(jSpinnerNbLinesOnTop, new XYConstraints(135, 66, 56, 19));
		jPanelLyricsLayout.add(jPanel4, new XYConstraints(10, 90, 205, 45));
		jPanel4.add(jLabel4, new XYConstraints(5, 5, -1, -1));
		jPanel4.add(jLabel12, new XYConstraints(5, 22, 162, -1));
		jPanel1.add(jLabel13, new XYConstraints(17, 25, 67, 19));
		jPanel1.add(jComboBoxCharsetHints, new XYConstraints(83, 25, 123, 19));
		jPanel1.add(jCheckBoxCharsetAutodetect, new XYConstraints(16, 2, 134, 21));
		jPanel1.add(jComboBoxCharsets, new XYConstraints(102, 65, 123, 19));
		jPanel1.add(jLabelManualCharset, new XYConstraints(16, 60, 79, 19));
		jPanel1.add(jPanel5, new XYConstraints(13, 97, 205, 45));
		jPanelLyrics.add(jPanelLyricsDisplay, new XYConstraints(18, 20, 206, 364));
		jPanel5.add(jLabel16, new XYConstraints(5, 5, -1, -1));
		jPanel5.add(jLabel17, new XYConstraints(5, 22, 162, -1));
		buttonGroup2.add(jRadioButtonGradiant);
		buttonGroup2.add(jRadioButtonMonochrome);
		buttonGroup2.add(jRadioButtonImage);
	}

	void jButtonChooseFont_actionPerformed(ActionEvent e)
	{
		fc.setSelectedFont(karaokePane.getCurentFont());
		fc.setVisible(true);
		Font f = fc.getSelectedFont();
		if (f != null)
		{
			karaokePane.setCurentFont(f);
			karaokePane.repaint();
			// update the preview panel
			// jPanelLyricsPreview.repaint();
		}

	}

	void jButtonClose_actionPerformed(ActionEvent e)
	{
		setVisible(false);
	}

	void jCheckBoxCharsetAutodetect_itemStateChanged(ItemEvent e)
	{
		jComboBoxCharsetHints.setEnabled(jCheckBoxCharsetAutodetect.isSelected());
		jComboBoxCharsets.setEnabled(!jCheckBoxCharsetAutodetect.isSelected());
		// karaokePane.setCharsetAutodetection(jCheckBox1.isSelected());
		charsetAutodetection = jCheckBoxCharsetAutodetect.isSelected();

		if (!jCheckBoxCharsetAutodetect.isSelected())
		{
			// we are not in autodetection mode
			karaokePane.props.charset = jComboBoxCharsets.getSelectedItem().toString();
			setCharset(jComboBoxCharsets.getSelectedItem().toString());
		}
	}

	public boolean getCharsetAutodetection()
	{
		return charsetAutodetection;
	}

	public void setCharsetAutodetection(boolean charsetAutodetection)
	{
		this.charsetAutodetection = charsetAutodetection;
	}

	public String getCharset()
	{
		return charset;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	void jComboBoxCharsets_actionPerformed(ActionEvent e)
	{
		setCharset(jComboBoxCharsets.getSelectedItem().toString());
		karaokePane.props.charset = jComboBoxCharsets.getSelectedItem().toString();
	}

	void jSpinnerNbColumns_stateChanged(ChangeEvent e)
	{
		karaokePane.props.cols = modelNbColumns.getNumber().intValue();
	}

	void jSpinnerNbRows_stateChanged(ChangeEvent e)
	{
		karaokePane.props.lines = modelNbrows.getNumber().intValue();
	}

	void jSpinnerNbLinesOnTop_stateChanged(ChangeEvent e)
	{
		karaokePane.props.readLine = modelNbReadLines.getNumber().intValue();
	}

	void jCheckBoxShadow_itemStateChanged(ItemEvent e)
	{
		karaokePane.props.displayShadow = jCheckBoxShadow.isSelected();
		jLabel7.setEnabled(jCheckBoxShadow.isSelected());
		colorSelectorShadow.setEnabled(jCheckBoxShadow.isSelected());
		jLabel9.setEnabled(jCheckBoxShadow.isSelected());
		jSliderShadowOpacity.setEnabled(jCheckBoxShadow.isSelected());
	}

	void jCheckBoxOutline_itemStateChanged(ItemEvent e)
	{
		karaokePane.props.displayOutline = jCheckBoxOutline.isSelected();
		jLabel14.setEnabled(jCheckBoxOutline.isSelected());
		// colorSelectorOutline.setEnabled(jCheckBoxShadow.isSelected());
	}

	void jSpinnerOutlineWidth_stateChanged(ChangeEvent e)
	{
		karaokePane.props.outlineWidth = modelOutlineWidth.getNumber().intValue();
	}

	void jRadioButtonGradiant_itemStateChanged(ItemEvent e)
	{
		if (jRadioButtonGradiant.isSelected())
		{
			if (karaokePane != null)
			{
				karaokePane.props.backgroundType = KaraokeProperties.BACKGROUND_GRADIANT;
				System.out.println("gradiant appelle redraw");
				karaokePane.redrawAll();
			}
		}

		jComboBoxRadiantType.setEnabled(jRadioButtonGradiant.isSelected());
		jLabel10.setEnabled(jRadioButtonGradiant.isSelected());
		jLabel11.setEnabled(jRadioButtonGradiant.isSelected());
		// colorSelectorGradiantStart.setEnabled(jRadioButtonGradiant.isSelected());
		// colorSelectorGradiantEnd.setEnabled(jRadioButtonGradiant.isSelected());
	}

	void jRadioButtonMonochrome_itemStateChanged(ItemEvent e)
	{
		if (jRadioButtonMonochrome.isSelected())
		{
			if (karaokePane != null)
			{
				karaokePane.props.backgroundType = KaraokeProperties.BACKGROUND_PLAIN;
				System.out.println("monoi appelle redraw");
				karaokePane.redrawAll();
			}
		}
		colorSelectorMonochrome.setEnabled(jRadioButtonMonochrome.isSelected());
	}

	void jRadioButtonImage_itemStateChanged(ItemEvent e)
	{
		if (jRadioButtonImage.isSelected())
		{
			if (karaokePane != null)
			{
				if (karaokePane.props.bgImage != null)
				{
					karaokePane.props.backgroundType = KaraokeProperties.BACKGROUND_IMAGE;
					System.out.println("image appelle redraw");
					karaokePane.redrawAll();
				}
			}
		}
		jButtonBrowseImage.setEnabled(jRadioButtonImage.isSelected());
		jTextFieldImageFilename.setEnabled(jRadioButtonImage.isSelected());
	}

	void jComboBoxCharsetHints_actionPerformed(ActionEvent e)
	{
		karaokePane.props.charsetHint = jComboBoxCharsetHints.getSelectedIndex();
	}

	void jButtonBrowseImage_actionPerformed(ActionEvent e)
	{
		// Open JFilechooser for selecting background image...
		if (chooser == null)
		{
			chooser = new JFileChooser();
		}

		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("jpg");
		filter.addExtension("gif");
		filter.setDescription("JPG & GIF Images");
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				karaokePane.props.bgImage = Toolkit.getDefaultToolkit().createImage(
				         chooser.getSelectedFile().getAbsolutePath());
				MediaTracker mt = new MediaTracker(this);
				mt.addImage(karaokePane.props.bgImage, 0);
				while (!mt.checkAll(true))
					try
					{
						Thread.sleep(20L);
					}
					catch (Exception exception)
					{}
				karaokePane.redrawAll();
			}
			catch (Exception ex)
			{
				System.out.println("Background image : " + karaokePane.props.bgImageFilename
				         + " does not exists");
			}

			karaokePane.props.bgImageFilename = chooser.getSelectedFile().getAbsolutePath();
			jTextFieldImageFilename.setText(chooser.getSelectedFile().getName());
		}

	}

}

class PreferencesDialog_jCheckBoxOutline_itemAdapter implements java.awt.event.ItemListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jCheckBoxOutline_itemAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.jCheckBoxOutline_itemStateChanged(e);
	}
}

class PreferencesDialog_jCheckBoxShadow_itemAdapter implements java.awt.event.ItemListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jCheckBoxShadow_itemAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.jCheckBoxShadow_itemStateChanged(e);
	}
}

class PreferencesDialog_jSpinnerNbLinesOnTop_changeAdapter implements
         javax.swing.event.ChangeListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jSpinnerNbLinesOnTop_changeAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void stateChanged(ChangeEvent e)
	{
		adaptee.jSpinnerNbLinesOnTop_stateChanged(e);
	}
}

class PreferencesDialog_jSpinnerNbColumns_changeAdapter implements javax.swing.event.ChangeListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jSpinnerNbColumns_changeAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void stateChanged(ChangeEvent e)
	{
		adaptee.jSpinnerNbColumns_stateChanged(e);
	}
}

class PreferencesDialog_jSpinnerNbRows_changeAdapter implements javax.swing.event.ChangeListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jSpinnerNbRows_changeAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void stateChanged(ChangeEvent e)
	{
		adaptee.jSpinnerNbRows_stateChanged(e);
	}
}

class PreferencesDialog_jButtonChooseFont_actionAdapter implements java.awt.event.ActionListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jButtonChooseFont_actionAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.jButtonChooseFont_actionPerformed(e);
	}
}

class PreferencesDialog_jComboBoxCharsets_actionAdapter implements java.awt.event.ActionListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jComboBoxCharsets_actionAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.jComboBoxCharsets_actionPerformed(e);
	}
}

class PreferencesDialog_jCheckBoxCharsetAutodetect_itemAdapter implements
         java.awt.event.ItemListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jCheckBoxCharsetAutodetect_itemAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.jCheckBoxCharsetAutodetect_itemStateChanged(e);
	}
}

class PreferencesDialog_jButtonClose_actionAdapter implements java.awt.event.ActionListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jButtonClose_actionAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.jButtonClose_actionPerformed(e);
	}
}

class PreferencesDialog_jSpinnerOutlineWidth_changeAdapter implements
         javax.swing.event.ChangeListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jSpinnerOutlineWidth_changeAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void stateChanged(ChangeEvent e)
	{
		adaptee.jSpinnerOutlineWidth_stateChanged(e);
	}
}

class PreferencesDialog_jRadioButtonGradiant_itemAdapter implements java.awt.event.ItemListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jRadioButtonGradiant_itemAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.jRadioButtonGradiant_itemStateChanged(e);
	}
}

class PreferencesDialog_jRadioButtonMonochrome_itemAdapter implements java.awt.event.ItemListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jRadioButtonMonochrome_itemAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.jRadioButtonMonochrome_itemStateChanged(e);
	}
}

class PreferencesDialog_jRadioButtonImage_itemAdapter implements java.awt.event.ItemListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jRadioButtonImage_itemAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.jRadioButtonImage_itemStateChanged(e);
	}
}

class PreferencesDialog_jComboBoxCharsetHints_actionAdapter implements
         java.awt.event.ActionListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jComboBoxCharsetHints_actionAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.jComboBoxCharsetHints_actionPerformed(e);
	}
}

class PreferencesDialog_jButtonBrowseImage_actionAdapter implements java.awt.event.ActionListener
{
	PreferencesDialog adaptee;

	PreferencesDialog_jButtonBrowseImage_actionAdapter(PreferencesDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.jButtonBrowseImage_actionPerformed(e);
	}
}
