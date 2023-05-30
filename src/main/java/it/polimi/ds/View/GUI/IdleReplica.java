package it.polimi.ds.View.GUI;

import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.Exceptions.FullDataStoreException;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class IdleReplica extends LayoutManager{

    private final DataStoreNetwork dsCommunication;

    public IdleReplica(String title, String topText, String bottomText, int myPort, String ip, int dataStorePort, int wQuorum, int rQuorum, Point point, Dimension size) throws QuorumNumberException, IOException {
        super(title, topText, bottomText);
        next = this;
        transition(point, size);

        dsCommunication = new DataStoreNetwork();
        dsCommunication.setPort(myPort);

        JLabel label = new JLabel();
        label.setFont(font);
        label.setForeground(Color.white);
        centralPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        centralPanel.add(label);

        if (wQuorum!=-1 && rQuorum!= -1 && dataStorePort==-1){
            bottomBanner.setText("DATASTORE INITIATED");
            label.setText("IP: "+ip+" PORT OPEN: "+ myPort +" wQuorum, rQuorum: "+wQuorum+" "+rQuorum);
            new Thread(() -> {
                try {
                    dsCommunication.initiateDataStore(wQuorum, rQuorum);
                } catch (IOException | QuorumNumberException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            bottomBanner.setText("done");

        }
        else if(wQuorum==-1 && rQuorum==-1){
            bottomBanner.setText("DATASTORE JOINED");
            label.setText("IP: "+ip+" PORT OPEN: "+myPort);

            new Thread(() -> {
                try {
                    //TODO: Remove hardcoded value
                    dsCommunication.joinDataStore(ip, dataStorePort);
                } catch (IOException | FullDataStoreException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            bottomBanner.setText("done");
        }
    }
}