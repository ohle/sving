module sving.core {
    requires java.desktop;
    requires java.logging;
    exports de.eudaemon.sving.core.manager;
    exports de.eudaemon.sving.core.testapp to sving.testapp;
}