package de.eudaemon.sving;

import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

class VMTableModel
        extends AbstractTableModel {

    private static final Logger LOG = Logger.getLogger(VMTableModel.class.getName());

    private List<VM> vms = new ArrayList<>();

    private final AgentManager agentManager;

    VMTableModel(AgentManager agentManager_) {
        agentManager = agentManager_;
        agentManager.addListener(this::attached);
        VMWatcher watcher = new VMWatcher();
        watcher.registerAddListener(this::addElement);
        watcher.registerRemoveListener(this::removeElement);
    }

    private void attached(VirtualMachineDescriptor descriptor) {
        for (int i = 0; i < vms.size(); i++) {
            if (vms.get(i).descriptor == descriptor) {
                fireTableCellUpdated(i, Column.ICON.ordinal());
                return;
            }
        }
    }

    private void addElement(VM vm) {
        vms.add(vm);
        fireTableRowsInserted(vms.size(), vms.size());
    }

    private void removeElement(VM vm) {
        int idx = vms.indexOf(vm);
        vms.remove(vm);
        fireTableRowsDeleted(idx, idx);
    }

    @Override
    public int getRowCount() {
        return vms.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        switch (Column.forIndex(column).orElseThrow(IllegalArgumentException::new)) {
            case ICON: return "";
            case NAME: return "Main class";
            default: throw new IllegalStateException();
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (Column.forIndex(columnIndex).orElseThrow(IllegalArgumentException::new)) {
            case ICON: return Icon.class;
            case NAME: return String.class;
            default: throw new IllegalStateException();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        VM vm = vms.get(rowIndex);
        switch (Column.forIndex(columnIndex).orElseThrow(IllegalArgumentException::new)) {
            case ICON:
                if (agentManager.isAttachedTo(vm.descriptor)) {
                    return Icon.ATTACHED.get(16);
                } else {
                    return new ImageIcon();
                }
            case NAME:
                return vm.toString();
            default:
                throw new IllegalStateException();
        }
    }

    VM get(int index) {
        return vms.get(index);
    }

    private enum Column {
        ICON, NAME;

        public static Optional<Column> forIndex(int idx) {
            return Stream.of(values()).filter(c -> c.ordinal() == idx).findFirst();
        }
    }
}
