package it.polimi.ds.View.GUI;

import it.polimi.ds.Client.Client;
import it.polimi.ds.DataStore.DataStoreState.DSElement;
import it.polimi.ds.View.CLI.InputValidation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class ClientScreen extends LayoutManager {

    private JButton readButton = new JButton();
    private JButton writeButton = new JButton();
    private JLabel keyLabel = new JLabel();
    private JLabel valueLabel = new JLabel();
    private JTextField keyField = new JTextField();
    private JTextField valueField = new JTextField();
    private JButton confirmOperation = new JButton();

    private boolean readClicked = false;
    private boolean writeClicked = false;

    private Client client;

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
                    // TODO: disattivare i campi e chiamare il resto
                    client = new Client();
                    client.bind(ip, Integer.parseInt(port));
                    centralPanel.removeAll();
                    bottomBanner.setText("CLIENT CONNECTED WITH "+ ip +" "+port);
                    readWrite();
                }
            }
        });
    }

    private void readWrite(){
        centralPanel.setLayout(new GridLayout(1,2));

        JPanel left = new JPanel();
        left.setLayout(new GridBagLayout());
        left.setOpaque(false);
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.gridx = 0;
        leftGbc.gridy = 0;
        leftGbc.insets = new Insets(20,0,20,0);
        leftGbc.anchor = GridBagConstraints.CENTER;
        leftGbc.weighty = 1.0;
        leftGbc.weightx = 1.0;
        readButton = new JButton("READ");
        buttonList.add(readButton);
        readButton.setFont(font);
        left.add(readButton, leftGbc);

        leftGbc.gridy = 1;
        writeButton = new JButton("WRITE");
        buttonList.add(writeButton);
        writeButton.setFont(font);
        left.add(writeButton, leftGbc);

        JPanel right = new JPanel();
        right.setLayout(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.insets = new Insets(0,0,0,0);
        rightGbc.anchor = GridBagConstraints.CENTER;
        rightGbc.weighty = 1.0;
        rightGbc.weightx = 1.0;

        keyLabel = new JLabel("Enter the key");
        keyLabel.setFont(font);
        right.add(keyLabel, rightGbc);

        rightGbc.gridy = 1;
        keyField = new JTextField();
        keyField.setFont(changeFont(fontPath,18));
        keyField.setPreferredSize(new Dimension(150, 30));
        keyField.setEnabled(false);
        right.add(keyField, rightGbc);

        rightGbc.gridy = 3;
        valueLabel = new JLabel("Enter the value");
        valueLabel.setFont(font);
        right.add(valueLabel, rightGbc);

        rightGbc.gridy = 4;
        valueField = new JTextField();
        valueField.setPreferredSize(new Dimension(150, 30));
        valueField.setFont(changeFont(fontPath,18));
        valueField.setEnabled(false);
        right.add(valueField, rightGbc);

        rightGbc.gridy = 5;
        confirmOperation = new JButton("CONFIRM");
        buttonList.add(confirmOperation);
        confirmOperation.setEnabled(false);
        confirmOperation.setFont(font);
        right.add(confirmOperation, rightGbc);

        centralPanel.add(left);
        centralPanel.add(right);

        readButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!readClicked && !writeClicked) {
                    readClicked = true;
                    writeButton.setEnabled(false);
                    keyField.setEnabled(true);
                    valueField.setEnabled(false);
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
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            bottomBanner.setText("read executed, value--" + dsElement.getValue()+"--version number--"+dsElement.getVersionNumber());
                            resetButtons();
                        }).start();

                    }
                }else if(writeClicked){
                    String text = keyField.getText();
                    String value = valueField.getText();

                    System.out.println("Key to write "+ text);
                    System.out.println("Value to writw "+ value);

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
        confirmOperation.setEnabled(false);
        keyField.setVisible(true);
        keyField.setEnabled(false);
        valueField.setVisible(true);
        valueField.setEnabled(false);
        keyLabel.setVisible(true);
        valueLabel.setVisible(true);
        keyField.setText("");
        valueField.setText("");
    }
}