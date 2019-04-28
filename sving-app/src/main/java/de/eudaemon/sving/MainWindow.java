package de.eudaemon.sving;

import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

class MainWindow
        extends JFrame {
    private static final Logger LOG = Logger.getLogger(MainWindow.class.getName());

    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(600, 400);
    private static final Dimension INFINITE_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final DefaultListSelectionModel vmSelection = new DefaultListSelectionModel();

    private final AgentManager agentManager;
    private final VMTableModel virtualMachines;

    MainWindow(AgentManager agentManager_) {
        agentManager = agentManager_;
        setLayout(new BorderLayout());
        setMinimumSize(MINIMUM_WINDOW_SIZE);
        virtualMachines = new VMTableModel(agentManager);
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

        JTable table = new JTable(virtualMachines);
        table.setSelectionModel(vmSelection);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setMaximumSize(INFINITE_SIZE);
        table.setShowGrid(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(0).setMaxWidth(20);
        table.getColumnModel().getColumn(1).setMinWidth(500);
        JScrollPane scrollPane= new JScrollPane(table);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        listPanel.add(scrollPane);
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
