package de.eudaemon.sving;

import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class VMTableModel
        extends AbstractTableModel {

    private List<VM> vms = new ArrayList<>();

    private final AgentManager agentManager;

    VMTableModel(AgentManager agentManager_, VMWatcher watcher) {
        agentManager = agentManager_;
        agentManager.addListener(this::attached);
        watcher.getVMs().forEach(this::addElement);
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
            case ICON:
                return ImageIcon.class;
            case NAME: return String.class;
            default: throw new IllegalStateException();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        VM vm = vms.get(rowIndex);
        switch (Column.forIndex(columnIndex).orElseThrow(IllegalArgumentException::new)) {
            case ICON:
                if (agentManager.isAutoAttach(vm.descriptor)) {
                    return Icon.STAR.get(16);
                }
                if (agentManager.isAttachedTo(vm.descriptor)) {
                    return Icon.ATTACHED.get(16);
                } else {
                    return null;
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
