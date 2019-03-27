package de.eudaemon.sving;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class VMListModel
        extends DefaultListModel<VMListModel.VM> {

    private static final int DELAY = 2000; // poll every 2 seconds
    private static final Logger log = Logger.getLogger(VMListModel.class.getName());

    private final Thread watcherThread = new Thread(this::poll, "VM Watcher");

    private Set<VM> descriptors = new HashSet<>();

    VMListModel() {
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
                log.warning("VM Polling thread interrupted!");
                break;
            }
        }
    }

    public static class VM {
        public final VirtualMachineDescriptor descriptor;

        public VM(VirtualMachineDescriptor descriptor_) {
            descriptor = descriptor_;
        }

        @Override
        public int hashCode() {
            return descriptor.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return descriptor.equals(obj);
        }

        @Override
        public String toString() {
            return descriptor.displayName();
        }
    }
}
