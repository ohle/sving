package de.eudaemon.sving;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import de.eudaemon.sving.core.manager.SvingWindowManager;
import de.eudaemon.util.UnanticipatedException;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

class AgentManager {

    private final File agentJar;
    private final URI coreJar;
    private static final Logger LOG = Logger.getLogger(AgentManager.class.getName());

    private final Map<VirtualMachineDescriptor, AttachedVM> attachedVMs = new HashMap<>();

    private final EventListenerList listeners = new EventListenerList();

    private final Preferences preferences;
    private final Set<String> autoAttachCommands = new HashSet<>();
    private static final String AUTOLOAD_COUNT = "auto-load-ids";
    private static final String AUTOLOAD_PREFIX = "auto-load-id-";

    private ErrorHandler errorHandler = new NullHandler();

    AgentManager(File agentJar_, VMWatcher watcher) {
        agentJar = agentJar_;
        preferences = Preferences.userRoot().node(getClass().getName());
        try {
            coreJar = SvingWindowManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e_) {
            throw new UnanticipatedException(e_);
        }
        loadAutoAttachIdsFromPreferences();
        watcher.registerAddListener(this::attachIfAuto);
    }

    private void loadAutoAttachIdsFromPreferences() {
        int count = preferences.getInt(AUTOLOAD_COUNT, 0);
        for (int i = 0; i < count; i++) {
            autoAttachCommands.add(preferences.get(AUTOLOAD_PREFIX + i, null));
        }
    }

    void attachTo(VirtualMachineDescriptor descriptor) {
        attachTo(descriptor, KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK));
    }

    void attachTo(VirtualMachineDescriptor descriptor, KeyStroke hotKey) {
        if (attachedVMs.containsKey(descriptor)) {
            LOG.log(Level.INFO, "Already attached - skipping");
            return;
        }
        try {
            VirtualMachine vm = VirtualMachine.attach(descriptor.id());
            vm.loadAgent(agentJar.getCanonicalPath(), coreJar.toString() + "|" + hotKey.toString());
            attachedVMs.put(descriptor, new AttachedVM(getCommandName(vm), hotKey));
            invokeForListeners(l -> l.attached(descriptor));
        } catch (AttachNotSupportedException e_) {
            String msg = "Attach not supported by target JVM!";
            errorHandler.error(msg);
            LOG.warning(msg);
        } catch (IOException e_) {
            throw new UnanticipatedException(e_);
        } catch (AgentLoadException e_) {
            String msg = "Error loading agent";
            errorHandler.error(msg);
            LOG.log(Level.SEVERE, msg, e_);
        } catch (AgentInitializationException e_) {
            String msg = "Error on agent initialization";
            errorHandler.error(msg);
            LOG.log(Level.SEVERE, msg, e_);
        }
    }

    private String getCommandName(VirtualMachine vm) {
        try {
            return vm.getSystemProperties().getProperty("sun.java.command");
        } catch (IOException e_) {
            throw new UnanticipatedException(e_);
        }
    }

    private Optional<String> getCommandName(VirtualMachineDescriptor descriptor) {
        try {
            VirtualMachine vm = VirtualMachine.attach(descriptor.id());
            return Optional.ofNullable(getCommandName(vm));
        } catch (AttachNotSupportedException | IOException e_) {
            return Optional.empty();
        }
    }

    void addAutoAttachTarget(VirtualMachineDescriptor vm) {
        Optional<String> cmd = getCommandName(vm);
        if (!cmd.isPresent()) {
            return;
        }
        if (autoAttachCommands.contains(cmd.get())) {
            return;
        }
        if (!isAttachedTo(vm)) {
            attachTo(vm);
        }
        autoAttachCommands.add(cmd.get());
        preferences.putInt(AUTOLOAD_COUNT, autoAttachCommands.size());
        preferences.put(AUTOLOAD_PREFIX + (autoAttachCommands.size() - 1), attachedVMs.get(vm).command);
    }

    public void removeAutoAttachTarget(VirtualMachineDescriptor vm) {
        if (!autoAttachCommands.contains(vm.id())) {
            return;
        }
        for (int i = 0; i < autoAttachCommands.size(); i++) {
            preferences.remove(AUTOLOAD_PREFIX + i);
        }
        autoAttachCommands.remove(vm.id());
        int i = 0;
        for (String autoAttachId : autoAttachCommands) {
            preferences.put(AUTOLOAD_PREFIX + i, autoAttachId);
            i++;
        }
    }

    private void attachIfAuto(VM vm) {
        getCommandName(vm.descriptor).map(autoAttachCommands::contains).ifPresent(p -> {
            if (!isAttachedTo(vm.descriptor)) {
                attachTo(vm.descriptor);
            }
        });
    }

    boolean isAutoAttach(VirtualMachineDescriptor vm) {
        return autoAttachCommands.contains(vm.id());
    }

    void addListener(Listener l) {
        listeners.add(Listener.class, l);
    }

    void removeListener(Listener l) {
        listeners.remove(Listener.class, l);
    }

    void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
    }

    private void invokeForListeners(Consumer<Listener> callback) {
        Arrays.stream(listeners.getListeners(Listener.class))
                .forEach(callback);
    }

    boolean isAttachedTo(VirtualMachineDescriptor descriptor) {
        return attachedVMs.containsKey(descriptor);
    }

    Optional<KeyStroke> getHotKey(VirtualMachineDescriptor descriptor) {
        return Optional.ofNullable(attachedVMs.get(descriptor).hotkey);
    }

    public interface Listener
            extends EventListener {
        void attached(VirtualMachineDescriptor descriptor);
    }

    public interface ErrorHandler {
        void error(String description);
    }

    private class NullHandler
            implements ErrorHandler {

        @Override
        public void error(String description) {
        }
    }

    private static final class AttachedVM {
        final String command;
        final KeyStroke hotkey;

        private AttachedVM(String command_, KeyStroke hotkey_) {
            command = command_;
            hotkey = hotkey_;
        }
    }
}
