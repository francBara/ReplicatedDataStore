package it.polimi.ds.View.GUI;

import javax.swing.*;

public class GUI {

    public static void main(String[] args){

        SwingUtilities.invokeLater(()->{
            LoadingScreen loadScreen = new LoadingScreen("Loading", "QUORUM-BASED REPLICATED DATASTORE", "LOADING");
            loadScreen.setVisible(true);
        });

    }
}
