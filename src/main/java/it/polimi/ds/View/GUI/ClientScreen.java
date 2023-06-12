package it.polimi.ds.View.GUI;

import it.polimi.ds.Client.Client;
import it.polimi.ds.Client.Exceptions.ReadException;
import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.View.CLI.InputValidation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class ClientScreen extends LayoutManager {

    private JButton readButton = new JButton();
    private JButton writeButton = new JButton();
    private JButton disconnect = new JButton();
    private JLabel keyLabel = new JLabel();
    private JLabel valueLabel = new JLabel();
    private JTextField keyField = new JTextField();
    private JTextField valueField = new JTextField();
    private JButton confirmOperation = new JButton();
    private boolean readClicked = false;
    private boolean writeClicked = false;
    private Client client;
    private boolean connected = false;


    public ClientScreen(String title, String topText, String bottomText) {
        super(title, topText, bottomText);

        JLabel ipLabel = new JLabel("datastore IP"); ipLabel.setFont(font);
        JLabel portLabel = new JLabel("datastore Port"); portLabel.setFont(font);
        JTextField ipAddress = new JTextField(); ipAddress.setPreferredSize(new Dimension(150,30)); ipAddress.setFont(font); ipAddress.setText("127.0.0.1");
        JTextField portNumber = new JTextField(); portNumber.setPreferredSize(new Dimension(150,30)); portNumber.setFont(font);
        centralPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        centralPanel.add(ipLabel, gbc);

        gbc.gridx++;
        centralPanel.add(ipAddress, gbc);
        gbc.gridx--;
        gbc.gridy++;
        centralPanel.add(portLabel, gbc);
        gbc.gridx++;
        centralPanel.add(portNumber, gbc);

        JButton connectButton = new JButton("Connect");
        connectButton.setFont(font);
        gbc.gridx--;
        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 0,0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        centralPanel.add(connectButton, gbc);


        InputValidation check = new InputValidation();
        connectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String ip = ipAddress.getText();
                String port = portNumber.getText();

                if (!(check.validateIp(ip) && check.validateInt(port))) {
                    bottomBanner.setText("INVALID");
                }
                else {
                    client = new Client();
                    client.bind(ip, Integer.parseInt(port));
                    centralPanel.removeAll();
                    previousPage.setText("Exit");
                    bottomBanner.setText("CLIENT CONNECTED WITH "+ ip +" "+port);
                    connected = true;
                    readWrite();
                }
            }
        });

        previousPage.setText("Back");
        previousPage.setFont(font);
        previousPage.setSize(new Dimension(80, 30));
        topPanel.add(previousPage, FlowLayout.LEFT);
        previousPage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!connected) {
                    SwingUtilities.invokeLater(() -> {
                        dispose();
                        next = new RoleScreen("Menu", "CHOOSE BETWEEN A CLIENT OR REPLICA", "Select the desired option");
                        transition(getLocation(), getSize());
                    });
                }else if(previousPage.getText().equals("NO")){
                    resetButtons();
                }
                else {
                    infoBanner.setText("Are you sure you want to disconnect?");
                    topPanel.add(disconnect, FlowLayout.RIGHT);
                    previousPage.setText("NO");
                }
                topPanel.revalidate();
                topPanel.repaint();
            }
        });

        disconnect.setText("YES");
        disconnect.setFont(font);
        disconnect.setSize(new Dimension(80, 30));
        disconnect.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    next = new RoleScreen("Menu", "CHOOSE BETWEEN A CLIENT OR REPLICA", "Select the desired option");
                    transition(getLocation(), getSize());
                });
            }
        });
    }

    private void readWrite(){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        centralPanel.setLayout(new GridBagLayout());
        readButton = new JButton("READ");
        buttonList.add(readButton);
        readButton.setFont(font);
        centralPanel.add(readButton, gbc);

        gbc.gridy++;
        writeButton = new JButton("WRITE");
        buttonList.add(writeButton);
        writeButton.setFont(font);
        centralPanel.add(writeButton, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridy++;
        keyLabel = new JLabel("Enter the key");
        keyLabel.setFont(font);
        centralPanel.add(keyLabel, gbc);

        gbc.gridy++;
        keyField = new JTextField();
        keyField.setFont(changeFont(18));
        keyField.setPreferredSize(new Dimension(150, 30));
        keyField.setEnabled(false);
        centralPanel.add(keyField, gbc);

        gbc.gridy++;
        valueLabel = new JLabel("Enter the value");
        valueLabel.setFont(font);
        centralPanel.add(valueLabel, gbc);

        gbc.gridy++;
        valueField = new JTextField();
        valueField.setPreferredSize(new Dimension(150, 30));
        valueField.setFont(changeFont(18));
        valueField.setEnabled(false);
        centralPanel.add(valueField, gbc);

        gbc.gridy++;
        confirmOperation.setText("CONFIRM");
        buttonList.add(confirmOperation);
        confirmOperation.setFont(font);
        confirmOperation.setEnabled(false);
        centralPanel.add(confirmOperation, gbc);

        readButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!readClicked && !writeClicked) {
                    readClicked = true;
                    writeButton.setEnabled(false);
                    keyField.setEnabled(true);
                    valueField.setVisible(false);
                    valueLabel.setVisible(false);
                    confirmOperation.setEnabled(true);
                    bottomBanner.setText("Now choose the key to read from");
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if(!writeClicked) {
                    writeButton.setEnabled(false);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(!readClicked) {
                    writeButton.setEnabled(true);
                }
            }
        });

        writeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!writeClicked && !readClicked) {
                    writeClicked = true;
                    readButton.setEnabled(false);
                    keyField.setEnabled(true);
                    valueField.setEnabled(true);
                    valueLabel.setEnabled(true);
                    confirmOperation.setEnabled(true);
                    bottomBanner.setText("Now choose the key to write and its value");
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if(!readClicked) {
                    readButton.setEnabled(false);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(!writeClicked) {
                    readButton.setEnabled(true);
                }
            }
        });

        confirmOperation.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(readClicked){
                    String text = keyField.getText();
                    System.out.println("Key to read "+ text);

                    if(text != null){
                        new Thread(()->{
                            DSElement dsElement;
                            try {
                                dsElement = client.read(text);
                            } catch (IOException | ReadException ex) {
                                throw new RuntimeException(ex);
                            }
                            if(dsElement.isNull()) {
                                bottomBanner.setText("This element is null!");
                            }else {
                                bottomBanner.setText("Read executed, value: " + dsElement.getValue() + ", version number: " + dsElement.getVersionNumber());
                            }
                            resetButtons();
                        }).start();

                    }
                }else if(writeClicked){
                    String text = keyField.getText();
                    String value = valueField.getText();

                    System.out.println("Key to write "+ text);
                    System.out.println("Value to write "+ value);

                    if(text != null && value != null){
                        new Thread( ()->{
                            boolean result;
                            try {
                                result = client.write(text, value);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            if (result) {
                                bottomBanner.setText("write executed");
                            } else {
                                bottomBanner.setText("write not executed");
                            }
                            resetButtons();
                        }).start();
                    }
                }
            }
        });
    }

    private void resetButtons(){
        writeClicked = false;
        readClicked = false;
        readButton.setEnabled(true);
        writeButton.setEnabled(true);
        keyField.setEnabled(false);
        keyLabel.setVisible(true);
        valueField.setEnabled(false);
        valueField.setVisible(true);
        valueLabel.setVisible(true);
        confirmOperation.setEnabled(false);
        confirmOperation.setVisible(true);
        keyField.setText("");
        valueField.setText("");
        previousPage.setText("Exit");
        infoBanner.setText("Perform a read or a write");
        topPanel.remove(disconnect);
        topPanel.revalidate();
        topPanel.repaint();
    }
}