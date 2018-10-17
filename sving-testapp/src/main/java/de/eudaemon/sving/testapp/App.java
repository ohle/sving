package de.eudaemon.sving.testapp;

import javax.swing.*;
import java.awt.*;

public class App {

    public App() {
        mainWindow = new JFrame("Sving test App");
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.setLayout(new BorderLayout());
        EventQueue.invokeLater(() -> {
            initComponents();
            mainWindow.pack();
            mainWindow.setVisible(true);
        });
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel first = new JPanel(new BorderLayout());
        JPanel mainPanel = createMainPanel();
        JPanel buttonPanel = createButtonPanel();
        first.add(mainPanel, BorderLayout.CENTER);
        first.add(buttonPanel, BorderLayout.SOUTH);

        JPanel second = new JPanel();
        second.add(new JLabel("Not much to see here"));

        tabbedPane.add("First", first);
        tabbedPane.add("Second", second);
        mainWindow.add(tabbedPane);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.gridx = 0; c.gridy = 0;
        c.weightx = 1;
        c.fill = 1;
        mainPanel.add(new TextField(), c);
        c.gridx = 1;
        mainPanel.add(new JCheckBox("Checkbox"), c);
        c.gridx = 0; c.gridy = 2;
        mainPanel.add(new JRadioButton("RadioButton"), c);
        c.gridx = 1;
        mainPanel.add(new JLabel("Just a label"), c);
        return mainPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton testButton = new JButton("test");
        JButton dialogButton = new JButton("Open dialog");
        dialogButton.addActionListener(e -> {
            JOptionPane.showInputDialog("What?");
        });
        testButton.addActionListener(e -> System.out.println("CLICK!"));
        panel.add(testButton);
        panel.add(dialogButton);
        return panel;
    }

    public static void main(String[] args) {
        App app = new App();
        app.start();
    }

    private final JFrame mainWindow;

    private void start() {

    }
}
