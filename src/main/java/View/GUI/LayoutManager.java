package View.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LayoutManager extends JFrame {

    protected JFrame next;
    protected JLabel infoBanner;
    protected JLabel bottomBanner;
    protected JPanel topPanel;
    protected JPanel centralPanel;
    protected JPanel bottomPanel;
    protected Font font;
    protected String title;
    protected String topText;
    protected String bottomText;
    protected String fontPath = "src/main/resources/Montserrat/static/Montserrat-ExtraBold.ttf";

    protected ArrayList<JButton> buttonList;

    public LayoutManager(String title, String topText, String bottomText){
        this.title = title;
        this.topText = topText;
        this.bottomText = bottomText;
        this.buttonList = new ArrayList<>();

        setTitle(this.title);
        setSize(980, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        this.font = changeFont(fontPath, 26);

        topPanel = new JPanel();
        topPanel.setBackground(Color.decode("#004360"));
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        infoBanner = new JLabel(this.topText);
        infoBanner.setFont(font);
        infoBanner.setForeground(Color.WHITE);
        topPanel.add(infoBanner);

        centralPanel = new JPanel();
        centralPanel.setLayout(new BorderLayout());
        centralPanel.setBackground(Color.decode("#02698a"));

        bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.decode("#004360"));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomBanner = new JLabel(this.bottomText);
        bottomBanner.setFont(font);
        bottomBanner.setForeground(Color.WHITE);
        bottomPanel.add(bottomBanner);

        setLayout(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();

        constr.fill = GridBagConstraints.BOTH;
        constr.weightx = 1;
        constr.weighty = 0.1;
        constr.gridheight = 1;

        constr.gridx = 0;
        constr.gridy = 0;
        add(topPanel, constr);

        constr.gridy = 1;
        constr.weighty = 0.8;
        add(centralPanel, constr);

        constr.gridy = 2;
        constr.weighty = 0.1;
        add(bottomPanel, constr);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeButtons(getWidth(), getHeight());
            }
        });
    }

    protected Font changeFont(String path, int size){
        Font style = null;
        try {
            File fontFile = new File(path);
            style = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(Font.PLAIN, size);
        }catch(IOException | FontFormatException e){
            e.printStackTrace();
        }
        return style;
    }

    private void resizeButtons(int width, int height) {
        int buttonWidth = width / 3;
        int buttonHeight = height / 8;
        Dimension buttonSize = new Dimension(buttonWidth, buttonHeight);

        for (JButton button : buttonList) {
            button.setPreferredSize(buttonSize);
            button.revalidate();
        }
    }

    protected void transition(Point currentLocation, Dimension currentSize){
        Dimension nextSize = new Dimension(currentSize.width, currentSize.height);
        next.setLocation(currentLocation);
        next.setSize(nextSize);
        next.setVisible(true);
    }
}