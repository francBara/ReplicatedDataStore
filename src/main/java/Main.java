import DataStore.DataStoreNetwork;
import DataStore.Exceptions.QuorumNumberException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class Main extends JFrame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("DataStore.Quorum-based replicated datastore");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel("Choose a port:");
        JTextField portInput = new JTextField("1883",10);
        JButton button = new JButton("OK");
        JButton choice1 = new JButton("Initialize data store");
        JButton choice2 = new JButton("Join data store");
        frame.setLayout(new FlowLayout());
        choice1.setEnabled(false);
        choice2.setEnabled(false);
        frame.add(choice1);
        frame.add(choice2);
        frame.add(label);
        frame.add(portInput);
        frame.add(button);
        frame.setVisible(true);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choice1.setEnabled(true);
                choice2.setEnabled(true);
            }
        });

        choice1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = Integer.parseInt(portInput.getText());
                System.out.println("Pulsante premuto! " + port);
                final DataStoreNetwork dsCommunication = new DataStoreNetwork();
                dsCommunication.setPort(port);
                System.out.println("DS creato!");
                try {
                    dsCommunication.initiateDataStore(5, 5);
                } catch(IOException | QuorumNumberException ex) {
                    System.out.println("ERROR");
                }
            }
        });

        choice2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = Integer.parseInt(portInput.getText());
                System.out.println("Pulsante premuto! " + port);
                final DataStoreNetwork dsCommunication = new DataStoreNetwork();
                dsCommunication.setPort(port);
                System.out.println("DS creato!");
                label.setText("Insert IP:");
                portInput.setText("127.0.0.1");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String ip = portInput.getText();
                        System.out.println(ip);
                        try {
                            dsCommunication.joinDataStore(ip, 5000);
                        } catch(IOException ex) {
                            System.out.println(ex);
                        }
                    }
                });

            }
        });
    }
}
