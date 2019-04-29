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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class AgentManager {

    private final File agentJar;
    private final URI coreJar;
    private static final Logger LOG = Logger.getLogger(AgentManager.class.getName());

    private final Map<VirtualMachineDescriptor, KeyStroke> attachedVMs = new HashMap<>();

    private final EventListenerList listeners = new EventListenerList();

    private ErrorHandler errorHandler = new NullHandler();

    AgentManager(File agentJar_) {
        agentJar = agentJar_;
        try {
            coreJar = SvingWindowManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e_) {
            throw new UnanticipatedException(e_);
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
            vm.loadAgent(agentJar.getCanonicalPath(), coreJar.toString());
            attachedVMs.put(descriptor, hotKey);
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
        return Optional.ofNullable(attachedVMs.get(descriptor));
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
}
