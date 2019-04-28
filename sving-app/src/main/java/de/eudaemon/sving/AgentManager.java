package de.eudaemon.sving;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import de.eudaemon.sving.core.manager.SvingWindowManager;
import de.eudaemon.util.UnanticipatedException;

import javax.swing.event.EventListenerList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class AgentManager {

    private final File agentJar;
    private final URI coreJar;
    private static final Logger LOG = Logger.getLogger(AgentManager.class.getName());

    private final Set<VirtualMachineDescriptor> attachedVMs = new HashSet<>();

    private final EventListenerList listeners = new EventListenerList();

    AgentManager(File agentJar_) {
        agentJar = agentJar_;
        try {
            coreJar = SvingWindowManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e_) {
            throw new UnanticipatedException(e_);
        }
    }

    void attachTo(VirtualMachineDescriptor descriptor) {
        if (attachedVMs.contains(descriptor)) {
            LOG.log(Level.INFO, "Already attached - skipping");
            return;
        }
        try {
            VirtualMachine vm = VirtualMachine.attach(descriptor.id());
            vm.loadAgent(agentJar.getCanonicalPath(), coreJar.toString());
            attachedVMs.add(descriptor);
            invokeForListeners(l -> l.attached(descriptor));
        } catch (AttachNotSupportedException e_) {
            LOG.warning("Attach not supported by target JVM!");
        } catch (IOException e_) {
            throw new UnanticipatedException(e_);
        } catch (AgentLoadException e_) {
            LOG.log(Level.SEVERE, "Error loading agent", e_);
        } catch (AgentInitializationException e_) {
            LOG.log(Level.SEVERE, "Error on agent initialization", e_);
        }
    }

    void addListener(Listener l) {
        listeners.add(Listener.class, l);
    }

    void removeListener(Listener l) {
        listeners.remove(Listener.class, l);
    }

    private void invokeForListeners(Consumer<Listener> callback) {
        Arrays.stream(listeners.getListeners(Listener.class))
                .forEach(callback);
    }

    boolean isAttachedTo(VirtualMachineDescriptor descriptor) {
        return attachedVMs.contains(descriptor);
    }

    public interface Listener
            extends EventListener {
        void attached(VirtualMachineDescriptor descriptor);
    }
}
