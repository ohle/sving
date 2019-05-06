package de.eudaemon.sving;

import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.logging.Logger;

class MainWindow
        extends JFrame {
    private static final Logger LOG = Logger.getLogger(MainWindow.class.getName());

    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(600, 400);
    private static final Dimension INFINITE_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final DefaultListSelectionModel vmSelection = new DefaultListSelectionModel();

    private final AgentManager agentManager;
    private final VMTableModel virtualMachines;

    private final HotKeyField hotKeyField = new HotKeyField();

    MainWindow(AgentManager agentManager_) {
        agentManager = agentManager_;
        agentManager.setErrorHandler(this::showAttachError);
        setLayout(new BorderLayout());
        setMinimumSize(MINIMUM_WINDOW_SIZE);
        virtualMachines = new VMTableModel(agentManager);
        add(createListPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
        registerHotkeyListener();
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

    private Container createButtonPanel() {
        Container buttonPanel = new Box(BoxLayout.X_AXIS);
        JButton attach = new JButton(new AttachAction());
        hotKeyField.setMinimumSize(new Dimension(50, 10));
        buttonPanel.add(new JLabel("Hotkey:"));
        buttonPanel.add(hotKeyField);
        buttonPanel.add(attach);
        buttonPanel.add(Box.createHorizontalGlue());
        return buttonPanel;
    }

    private void registerHotkeyListener() {
        vmSelection.addListSelectionListener(e ->
                getSelectedVM().ifPresent(vm ->
                        hotKeyField.setKeyStroke(agentManager.getHotKey(vm).orElse(HotKeyField.DEFAULT_HOTKEY))
                ));
    }

    private void showAttachError(String message) {
        JOptionPane.showMessageDialog(
                this,
                String.format("%s\nPlease check the output or logs of the target application.", message),
                "Couldn't attach",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private Optional<VirtualMachineDescriptor> getSelectedVM() {
        int selectionIndex = vmSelection.getAnchorSelectionIndex();
        if (selectionIndex > 0) {
            return Optional.ofNullable(virtualMachines.get(selectionIndex)).map(vm -> vm.descriptor);
        } else {
            return Optional.empty();
        }
    }

    private final class AttachAction
            extends AbstractAction {

        AttachAction() {
            putValue(NAME, "Attach");
            putValue(SHORT_DESCRIPTION, "Attach to selected JVM");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getSelectedVM().ifPresent(vm -> agentManager.attachTo(vm, hotKeyField.getKeyStroke()));
        }
    }
}
