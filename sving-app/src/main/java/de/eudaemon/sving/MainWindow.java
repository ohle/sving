package de.eudaemon.sving;

import javax.swing.*;
import java.awt.*;

class MainWindow
        extends JFrame {

    MainWindow() {
        setLayout(new BorderLayout());
        JList<VMListModel.VM> vmList = new JList<>(new VMListModel());
        vmList.setCellRenderer(new DefaultListCellRenderer());
        add(vmList, BorderLayout.CENTER);
    }
}
