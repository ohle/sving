package de.eudaemon.sving;

import java.util.logging.ConsoleHandler;

public class SystemOutHandler
        extends ConsoleHandler {
    public void init() {
        setOutputStream(System.out);
    }
}
