package it.polimi.ds.View.GUI;

import it.polimi.ds.DataStore.DataStoreNetwork;
import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;


public class IdleReplica extends LayoutManager{

    private final DataStoreNetwork dsCommunication;

    public IdleReplica(String title, String topText, String bottomText, String ip, int port, int wQuorum, int rQuorum, Point point, Dimension size) throws QuorumNumberException, IOException {
        super(title, topText, bottomText);
        next = this;
        transition(point, size);

        dsCommunication = new DataStoreNetwork();
        //TODO: Insert server port
        dsCommunication.setPort(port);

        JLabel label = new JLabel();
        label.setFont(font);
        label.setForeground(Color.white);
        centralPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        centralPanel.add(label);

        if (wQuorum!=-1 && rQuorum!= -1){
            bottomBanner.setText("DATASTORE INITIATED");
            label.setText("IP: "+ip+" PORT OPEN: "+port+" wQuorum, rQuorum: "+wQuorum+" "+rQuorum);

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
            label.setText("IP: "+ip+" PORT OPEN: "+port);

            new Thread(() -> {
                try {
                    //TODO: Remove hardcoded value
                    dsCommunication.joinDataStore(ip, 5000);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            bottomBanner.setText("done");
        }
    }
}