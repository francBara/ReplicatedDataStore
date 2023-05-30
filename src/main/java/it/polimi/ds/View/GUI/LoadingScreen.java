package it.polimi.ds.View.GUI;

import javax.swing.*;
import java.awt.*;

public class LoadingScreen extends LayoutManager {
    public LoadingScreen(String title, String topText, String bottomText) {
        super(title, topText, bottomText);

        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image originalImage = originalIcon.getImage();

        JLabel imageLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                int maxWidth = centralPanel.getWidth();
                int maxHeight = centralPanel.getHeight();

                int imageWidth = originalImage.getWidth(null);
                int imageHeight = originalImage.getHeight(null);

                double widthRatio = (double) maxWidth / imageWidth;
                double heightRatio = (double) maxHeight / imageHeight;

                double scaleRatio = Math.min(widthRatio, heightRatio);

                int scaledWidth = (int) (imageWidth * scaleRatio);
                int scaledHeight = (int) (imageHeight * scaleRatio);

                int x = (maxWidth - scaledWidth) / 2;
                int y = (maxHeight - scaledHeight) / 2;

                g2.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);

                g2.dispose();
            }
        };
        centralPanel.add(imageLabel, BorderLayout.CENTER);

        //timer start
        int totalExecutions = 1;
        final int[] executionCount = {0};

        Timer timer = new Timer(1000, e -> {
            executionCount[0]++;

            if (executionCount[0] >= totalExecutions) {
                ((Timer) e.getSource()).stop();

                SwingUtilities.invokeLater(()->{
                    dispose();
                    next = new RoleScreen("Menu", "CHOOSE BETWEEN A CLIENT OR REPLICA", "Select the desired option");
                    transition(getLocation(),getSize());
                });
            }
            bottomBanner.setText(".".repeat(executionCount[0] % 4)+ " " + this.bottomText + " " + ".".repeat(executionCount[0] % 4));
        });
        timer.start();
    }
}