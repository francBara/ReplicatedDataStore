package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoleScreen extends LayoutManager {
    private final JButton clientButton;
    private final JButton replicaButton;
    private final JPanel buttonPanel;
    private String oldBottomText;

    public RoleScreen(String title, String topText, String bottomText) {
        super(title, topText, bottomText);

        clientButton = new JButton("CLIENT");
        clientButton.setFont(font);
        buttonList.add(clientButton);
        replicaButton = new JButton("REPLICA");
        replicaButton.setFont(font);
        buttonList.add(replicaButton);
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        centralPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20,0,20,0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        buttonPanel.add(clientButton, gbc);

        gbc.gridy++;
        buttonPanel.add(replicaButton, gbc);
        centralPanel.add(buttonPanel);


        clientButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                replicaButton.setEnabled(false);
                oldBottomText = bottomBanner.getText();
                bottomBanner.setText("Can read and write data from/to the datastore");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bottomBanner.setText(oldBottomText);
                replicaButton.setEnabled(true);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                next = new ClientScreen("DataStore", "Perform a read or a write", "Select the desired option");
                transition(getLocation(), getSize());
            }
        });

        replicaButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                clientButton.setEnabled(false);
                oldBottomText = bottomBanner.getText();
                bottomBanner.setText("Can initiate or join a datastore");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bottomBanner.setText(oldBottomText);
                clientButton.setEnabled(true);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                next = new ReplicaScreen("DataStore", "Initiate or join a DataStore", "");
                transition(getLocation(), getSize());
            }
        });
    }
}