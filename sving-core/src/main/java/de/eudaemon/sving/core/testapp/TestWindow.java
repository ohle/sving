package de.eudaemon.sving.core.testapp;

import javax.swing.*;
import java.awt.*;

public class TestWindow
        extends JFrame {

    public TestWindow() {
        super("Sving test GUI");
        setLayout(new BorderLayout());
        setName("test-app-frame");
        initComponents();
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
        add(tabbedPane);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.gridx = 0; c.gridy = 0;
        c.weightx = 1;
        c.fill = 1;
        JTextField textField = new JTextField();
        textField.setName("input");
        mainPanel.add(textField, c);
        c.gridx = 1;
        JCheckBox checkbox = new JCheckBox("Checkbox");
        checkbox.setName("checkbox");
        mainPanel.add(checkbox, c);
        c.gridx = 0; c.gridy = 2;
        JRadioButton radioButton = new JRadioButton("RadioButton");
        radioButton.setName("radio-button");
        mainPanel.add(radioButton, c);
        c.gridx = 1;
        JLabel label = new JLabel("Just a label");
        label.setName("label");
        mainPanel.add(label, c);
        return mainPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton testButton = new JButton("test");
        testButton.setName("test-button");
        JButton dialogButton = new JButton("Open dialog");
        dialogButton.setName("dialog-button");
        dialogButton.addActionListener(e -> {
            JOptionPane.showInputDialog(this , "What?");
        });
        testButton.addActionListener(e -> System.out.println("CLICK!"));
        panel.add(testButton);
        panel.add(dialogButton);
        return panel;
    }

}
