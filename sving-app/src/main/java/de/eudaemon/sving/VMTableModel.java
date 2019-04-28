package de.eudaemon.sving;

import com.sun.tools.attach.VirtualMachine;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class VMTableModel
        extends AbstractTableModel {

    private static final int DELAY = 2000; // poll every 2 seconds
    private static final Logger LOG = Logger.getLogger(VMTableModel.class.getName());

    private Set<VM> descriptors = new HashSet<>();
    private List<VM> vms = new ArrayList<>();

    private final AgentManager agentManager;

    VMTableModel(AgentManager agentManager_) {
        agentManager = agentManager_;
        agentManager.addListener(this::attached);
        Thread watcherThread = new Thread(this::poll, "VM Watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private void poll() {
        while (true) {
            Set<VM> currentDescriptors = VirtualMachine.list().stream().map(VM::new).collect(Collectors.toSet());
            Set<VM> toRemove = new HashSet<>(descriptors);
            toRemove.removeAll(currentDescriptors);
            toRemove.forEach(this::removeElement);
            currentDescriptors.removeAll(descriptors);
            currentDescriptors.forEach(d -> {
                descriptors.add(d);
                addElement(d);
            });
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e_) {
                LOG.warning("VM Polling thread interrupted!");
                break;
            }
        }
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
                    return getIcon("attached");
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

    public static class VM {
        final VirtualMachineDescriptor descriptor;

        VM(VirtualMachineDescriptor descriptor_) {
            descriptor = descriptor_;
        }

        @Override
        public int hashCode() {
            return descriptor.hashCode();
        }

        @Override
        public boolean equals(Object o_) {
            if (this == o_) return true;
            if (o_ == null || getClass() != o_.getClass()) return false;
            VM vm = (VM) o_;
            return Objects.equals(descriptor, vm.descriptor);
        }

        @Override
        public String toString() {
            return descriptor.displayName();
        }
    }

    private enum Column {
        ICON, NAME;

        public static Optional<Column> forIndex(int idx) {
            return Stream.of(values()).filter(c -> c.ordinal() == idx).findFirst();
        }
    }

    private Map<String, Icon> icons = new HashMap<>();

    private Icon getIcon(String name) {
        return icons.computeIfAbsent(name, this::loadIcon);
    }

    private Icon loadIcon(String name) {
        InputStream image = MainWindow.class.getClassLoader().getResourceAsStream(name + ".png");
        assert image != null;
        try {
            return new ImageIcon(ImageIO.read(image));
        } catch (IOException e_) {
            LOG.log(Level.WARNING, "Couldn't load icon '" + name + "'");
            return new ImageIcon();
        }
    }
}
