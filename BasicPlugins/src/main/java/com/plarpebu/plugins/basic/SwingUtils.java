package com.plarpebu.plugins.basic;

import java.awt.Insets;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

public class SwingUtils
{
	/**
	 * Permet de créer un nouveau bouton en lui associant, une action, un tooltip et une icone
	 * 
	 * @param p
	 *           composant dans lequel il va etre ajouté
	 * @param name
	 *           son nom pour lui associer une action(actionPerformed)
	 * @param tooltiptext
	 *           le tooltip à afficher
	 * @param imageName
	 *           le chemin d'accés à l'icone
	 */
	public static JButton addButton(JComponent p, String name, String tooltiptext, String imageName)
	{
		JButton b;
		if ((imageName == null) || (imageName.equals("")))
		{
			b = (JButton) p.add(new JButton(name));
		}
		else
		{
			URL u = p.getClass().getResource(imageName);
			if (u != null)
			{
				ImageIcon im = new ImageIcon(u);

				b = (JButton) p.add(new JButton(im));
			}
			else
			{
				b = (JButton) p.add(new JButton(name));
			}
		}

		b.setToolTipText(tooltiptext);
		Insets insets = new Insets(0, 0, 0, 0);
		b.setMargin(insets);

		return b;
	}
}
