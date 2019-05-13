package de.eudaemon.sving;

import com.sun.tools.attach.VirtualMachine;
import de.eudaemon.sving.util.ListenerRegistry;

import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class VMWatcher {

    private static final int DELAY = 2000; // poll every 2 seconds

    private Set<VM> descriptors = new HashSet<>();

    private final ListenerRegistry<AddListener> addListeners = new ListenerRegistry<>();
    private final ListenerRegistry<RemoveListener> removeListeners = new ListenerRegistry<>();

    private static final Logger LOG = Logger.getLogger(VMWatcher.class.getName());

    VMWatcher() {
        Thread watcherThread = new Thread(this::poll, "VM Watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    void registerAddListener(AddListener listener) {
        addListeners.add(listener);
    }

    void registerRemoveListener(RemoveListener listener) {
        removeListeners.add(listener);
    }

    Stream<VM> getVMs() {
        return descriptors.stream();
    }

    private void poll() {
        while (true) {
            Set<VM> currentDescriptors = VirtualMachine.list().stream().map(VM::new).collect(Collectors.toSet());
            Set<VM> toRemove = new HashSet<>(descriptors);
            toRemove.removeAll(currentDescriptors);
            toRemove.forEach(vm -> removeListeners.invoke(l -> l.vmRemoved(vm)));
            currentDescriptors.removeAll(descriptors);
            currentDescriptors.forEach(d -> {
                descriptors.add(d);
                addListeners.invoke(l -> l.vmAdded(d));
            });
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e_) {
                LOG.warning("VM Polling thread interrupted!");
                break;
            }
        }
    }

    interface AddListener extends EventListener {
        void vmAdded(VM vm);
    }

    interface RemoveListener extends EventListener {
        void vmRemoved(VM vm);
    }
}
