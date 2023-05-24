package it.polimi.ds.View.GUI;

import it.polimi.ds.DataStore.Exceptions.QuorumNumberException;
import it.polimi.ds.View.CLI.InputValidation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;


public class ReplicaScreen extends LayoutManager {
    private final JButton initiateButton;
    private final JButton joinButton;
    private final JButton confirmButton;
    private final JTextField writeQuorum; private final JLabel wQuorumLabel;
    private final JTextField readQuorum;private final JLabel rQuorumLabel;
    private final JTextField ipAddress;private final JLabel ipLabel;
    private final JTextField portNumber;private final JLabel portLabel;
    private boolean joinClicked = false;
    private boolean initiateClicked = false;


    public ReplicaScreen(String title, String topText, String bottomText) {
        super(title, topText, bottomText);
        ArrayList<JTextField> textFields = new ArrayList<>();

        centralPanel.setLayout(new GridLayout(2,1));

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new GridBagLayout());
        upperPanel.setOpaque(false);

        GridBagConstraints upperGbc = new GridBagConstraints();
        upperGbc.gridx = 0;
        upperGbc.gridy = 0;
        upperGbc.anchor = GridBagConstraints.CENTER;
        upperGbc.insets = new Insets(0, 0, 0, 0);

        initiateButton = new JButton("INITIATE");
        buttonList.add(initiateButton);
        initiateButton.setFont(font);
        upperPanel.add(initiateButton, upperGbc);

        upperGbc.gridy = 1;
        joinButton = new JButton("JOIN");
        buttonList.add(joinButton);
        joinButton.setFont(font);
        upperPanel.add(joinButton, upperGbc);

        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new GridBagLayout());
        lowerPanel.setOpaque(false);

        writeQuorum = new JTextField(); writeQuorum.setFont(font); writeQuorum.setPreferredSize(new Dimension(150, 30)); writeQuorum.setVisible(false);
        readQuorum = new JTextField(); readQuorum.setFont(font); readQuorum.setPreferredSize(new Dimension(150, 30)); readQuorum.setVisible(false);
        ipAddress = new JTextField(); ipAddress.setFont(font); ipAddress.setPreferredSize(new Dimension(150, 30)); ipAddress.setVisible(false);
        portNumber = new JTextField(); portNumber.setFont(font); portNumber.setPreferredSize(new Dimension(150, 30)); portNumber.setVisible(false);

        textFields.add(writeQuorum); textFields.add(readQuorum); textFields.add(ipAddress); textFields.add(portNumber);

        wQuorumLabel = new JLabel("Write quorum number"); wQuorumLabel.setFont(font); wQuorumLabel.setVisible(false);
        rQuorumLabel = new JLabel("Read quorum number"); rQuorumLabel.setFont(font); rQuorumLabel.setVisible(false);
        ipLabel = new JLabel("Ip address"); ipLabel.setFont(font); ipLabel.setVisible(false);
        portLabel = new JLabel("Port Number"); portLabel.setFont(font); portLabel.setVisible(false);

        GridBagConstraints lowerGbc = new GridBagConstraints();
        lowerGbc.gridx = 0;
        lowerGbc.gridy = 0;
        lowerGbc.insets = new Insets(10, 0, 5,20);

        lowerGbc.gridy++;
        lowerPanel.add(ipLabel, lowerGbc);
        lowerGbc.gridx++;
        lowerPanel.add(ipAddress, lowerGbc);
        lowerGbc.gridx--;

        lowerGbc.gridy++;
        lowerPanel.add(portLabel, lowerGbc);
        lowerGbc.gridx++;
        lowerPanel.add(portNumber, lowerGbc);
        lowerGbc.gridx--;

        lowerGbc.gridy++;
        lowerPanel.add(wQuorumLabel, lowerGbc);
        lowerGbc.gridx++;
        lowerPanel.add(writeQuorum, lowerGbc);
        lowerGbc.gridx--;

        lowerGbc.gridy++;
        lowerPanel.add(rQuorumLabel, lowerGbc);
        lowerGbc.gridx++;
        lowerPanel.add(readQuorum, lowerGbc);
        lowerGbc.gridx--;

        confirmButton = new JButton("CONFIRM");
        buttonList.add(confirmButton);
        confirmButton.setVisible(false);
        confirmButton.setEnabled(false);
        confirmButton.setFont(font);
        lowerGbc.gridy++;
        lowerGbc.insets = new Insets(30, 0, 0,0);
        lowerGbc.gridwidth = GridBagConstraints.REMAINDER;
        lowerGbc.anchor = GridBagConstraints.CENTER;
        lowerPanel.add(confirmButton, lowerGbc);

        centralPanel.add(upperPanel);
        centralPanel.add(lowerPanel);

        initiateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!initiateClicked && !joinClicked){
                    initiateClicked = true;
                    joinButton.setEnabled(false);
                    ipLabel.setVisible(true);
                    ipAddress.setVisible(true);
                    ipAddress.setText("127.0.0.1");
                    ipAddress.setEnabled(false);
                    portLabel.setVisible(true);
                    portNumber.setVisible(true);
                    confirmButton.setVisible(true);
                    confirmButton.setEnabled(true);
                    wQuorumLabel.setVisible(true);
                    writeQuorum.setVisible(true);
                    rQuorumLabel.setVisible(true);
                    readQuorum.setVisible(true);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if(!joinClicked){
                    joinButton.setEnabled(false);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(!initiateClicked) {
                    joinButton.setEnabled(true);
                }
            }
        });

        joinButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!initiateClicked && !joinClicked){
                    joinClicked = true;
                    initiateButton.setEnabled(false);
                    ipLabel.setVisible(true);
                    ipAddress.setVisible(true);
                    portLabel.setVisible(true);
                    portNumber.setVisible(true);
                    confirmButton.setVisible(true);
                    confirmButton.setEnabled(true);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if(!initiateClicked) {
                    initiateButton.setEnabled(false);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(!joinClicked){
                    initiateButton.setEnabled(true);
                }
            }
        });


        InputValidation check = new InputValidation();
        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String ip = ipAddress.getText();
                String port = portNumber.getText();
                String wQuorum = writeQuorum.getText();
                String rQuorum = readQuorum.getText();

                if (initiateClicked) {
                    if (!(check.validateIp(ip) && check.validateInt(port) && check.validateInt(wQuorum) && check.validateInt(rQuorum))) {
                        bottomBanner.setText("INVALID");
                    }
                    else {
                        dispose();
                        Point point = getLocation();
                        Dimension size = getSize();
                        SwingUtilities.invokeLater(()->{
                            try {
                                next = new IdleReplica("DsCommunication", "FIRST REPLICA", "", ip, Integer.parseInt(port), Integer.parseInt(wQuorum), Integer.parseInt(rQuorum), point, size);
                            } catch (QuorumNumberException | IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    }
                }
                else if (joinClicked) {
                    if (!(check.validateIp(ip) && check.validateInt(port))){
                        bottomBanner.setText("INVALID");
                    }else{
                        dispose();
                        Point point = getLocation();
                        Dimension size = getSize();

                        SwingUtilities.invokeLater(()->{
                            try {
                                next = new IdleReplica("DsCommunication", "JOINED REPLICA", "",ip, Integer.parseInt(port), -1, -1, point, size);
                            } catch (QuorumNumberException | IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });

                    }
                }
            }
        });
    }
}