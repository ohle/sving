package de.eudaemon.sving;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

class MainWindow
        extends JFrame {

    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(600, 400);
    private static final Dimension INFINITE_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final VMListModel virtualMachines = new VMListModel();
    private final DefaultListSelectionModel vmSelection = new DefaultListSelectionModel();

    private final AgentManager agentManager;

    MainWindow(AgentManager agentManager_) {
        agentManager = agentManager_;
        setLayout(new BorderLayout());
        setMinimumSize(MINIMUM_WINDOW_SIZE);
        add(createListPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createListPanel() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        JLabel header = new JLabel("Running JVMs:");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setPreferredSize(new Dimension(header.getWidth(), header.getHeight() + 30));
        listPanel.add(header);

        JList<VMListModel.VM> vmList = new JList<>(virtualMachines);
        vmList.setSelectionModel(vmSelection);
        vmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vmList.setCellRenderer(new DefaultListCellRenderer());
        vmList.setMaximumSize(INFINITE_SIZE);
        JScrollPane scrollPane= new JScrollPane(vmList);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        listPanel.add(scrollPane);
        return listPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        JButton attach = new JButton("Attach");
        attach.addActionListener(e -> agentManager.attachTo(getSelectedVM()));
        buttonPanel.add(attach);
        return buttonPanel;
    }

    private VirtualMachineDescriptor getSelectedVM() {
        return virtualMachines.get(vmSelection.getAnchorSelectionIndex()).descriptor;
    }

}
