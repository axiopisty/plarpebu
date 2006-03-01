package com.plarpebu.plugins.albumgrabber;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

public class SearchFrame extends JFrame {

    private BorderLayout borderLayout1 = new BorderLayout();

    private JFormattedTextField auteurT = null;

    private JFormattedTextField albumT = null;

    private JLabel nomAuteur = null;

    private JLabel nomAlbum = null;

    private AlbumGrabberPlugin abp = null;

    private String auteurS = null;

    private String albumS = null;

    private Container pane = null;

    private JButton gosearch = null;

    public SearchFrame(AlbumGrabberPlugin abp) {
        this.abp = abp;
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        pane = this.getContentPane();
        pane.setLayout(borderLayout1);

        auteurS = abp.getAuteur();
        albumS = abp.getAlbum();

        nomAuteur = new JLabel("Nom de l'auteur : ");
        nomAlbum = new JLabel("Nom de l'album : ");

        auteurT = new JFormattedTextField();
        auteurT.setColumns(30);

        if (auteurS != null)
            auteurT.setValue(auteurS);
        else
            auteurT.setValue("null");

        albumT = new JFormattedTextField();

        if (albumS != null)
            albumT.setValue(albumS);
        else
            albumT.setValue("null");

        albumT.setColumns(30);

        JPanel labelPane = new JPanel(new GridLayout(0, 1));
        labelPane.add(nomAuteur);
        labelPane.add(nomAlbum);

        JPanel fieldPane = new JPanel(new GridLayout(0, 1));
        fieldPane.add(auteurT);
        fieldPane.add(albumT);

        gosearch = new JButton("Go search");
        gosearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("on lance la recherche");
                auteurS = (String) auteurT.getValue();
                albumS = (String) albumT.getValue();
                System.out.println(auteurS + albumS);
                abp.setAlbum(auteurS, albumS);
            }
        });

        pane.add(labelPane, BorderLayout.CENTER);
        pane.add(fieldPane, BorderLayout.LINE_END);
        pane.add(gosearch, BorderLayout.SOUTH);

        // abp.setAlbum(auteurS, albumS);

        setTitle("Search Frame");
        setSize(new Dimension(350, 100));
        pack();
    }

}
