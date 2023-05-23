package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientScreen extends LayoutManager {

    private final JButton readButton;
    private final JButton writeButton;
    private final JLabel keyLabel;
    private final JLabel valueLabel;
    private final JTextField keyField;
    private final JTextField valueField;
    private final JButton confirmOperation;
    private boolean readClicked = false;
    private boolean writeClicked = false;
    public ClientScreen(String title, String topText, String bottomText) {
        super(title, topText, bottomText);

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
        DefaultListModel<String> keySuggestions = new DefaultListModel<>();
        JList<String> keyList = new JList<>(keySuggestions);
        keyList.setFont(changeFont(fontPath,15));
        right.add(keyField, rightGbc);

        rightGbc.gridy = 2;
        JScrollPane keyScrollPane = new JScrollPane(keyList);
        keyScrollPane.setEnabled(false);
        keyScrollPane.setVisible(false);
        right.add(keyScrollPane, rightGbc);

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


        ArrayList<String> suggestions = new ArrayList<>(Arrays.asList("apple", "banana", "orange", "pineapple", "luca", "francesco", "Olbia", "Sardegna"));

        keyField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keySuggestions.clear();

                String searchText = keyField.getText().toLowerCase();

                for(String suggestion: suggestions){
                    if(suggestion.toLowerCase().startsWith(searchText)){
                        keySuggestions.addElement(suggestion);
                    }
                }
            }
        });

        keyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = keyList.getSelectedValue();
                if (selected!=null){
                    keyField.setText(selected);
                    //keySuggestions.clear();
                }
            }
        });

        keyField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for(String suggestion: suggestions){
                        keySuggestions.addElement(suggestion);
                }
            }
        });

        centralPanel.add(left);
        centralPanel.add(right);

        readButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!readClicked && !writeClicked) {
                    readClicked = true;
                    writeButton.setEnabled(false);
                    keyField.setEnabled(true);
                    keyScrollPane.setVisible(true);
                    keyScrollPane.setEnabled(true);

                    valueField.setEnabled(false);
                    valueField.setVisible(false);
                    valueLabel.setVisible(false);
                    bottomBanner.setText("Now choose the key to read from");
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                writeButton.setEnabled(false);
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
                    keyScrollPane.setVisible(true);
                    keyScrollPane.setEnabled(true);
                    valueField.setEnabled(true);
                    bottomBanner.setText("Now choose the key to write and its value");
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                readButton.setEnabled(false);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if(!writeClicked) {
                    readButton.setEnabled(true);
                }
            }
        });
    }
}