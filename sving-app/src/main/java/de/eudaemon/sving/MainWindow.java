package de.eudaemon.sving;

import javax.swing.*;
import java.awt.*;

class MainWindow
        extends JFrame {

    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(600, 400);
    private static final Dimension INFINITE_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    MainWindow() {
        setLayout(new BorderLayout());
        setMinimumSize(MINIMUM_WINDOW_SIZE);
        add(createListPanel(), BorderLayout.CENTER);
    }

    private JPanel createListPanel() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        JLabel header = new JLabel("Running JVMs:");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setPreferredSize(new Dimension(header.getWidth(), header.getHeight() + 30));
        listPanel.add(header);

        JList<VMListModel.VM> vmList = new JList<>(new VMListModel());
        vmList.setCellRenderer(new DefaultListCellRenderer());
        vmList.setMaximumSize(INFINITE_SIZE);
        JScrollPane scrollPane= new JScrollPane(vmList);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        listPanel.add(scrollPane);
        return listPanel;
    }
}
