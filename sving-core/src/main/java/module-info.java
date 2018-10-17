module sving.core {
    requires java.desktop;
    requires java.logging;
    exports de.eudaemon.sving.core.manager to sving.app, sving.agent;
}