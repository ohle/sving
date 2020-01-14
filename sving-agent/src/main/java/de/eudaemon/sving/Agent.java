package de.eudaemon.sving;


import de.eudaemon.sving.core.manager.SvingWindowManager;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

public class Agent {
    public static void agentmain(String options, Instrumentation inst) {
        String[] option = options.split("\\|");
        String jarUri = option[0];
        String hotKey = option.length > 1 ? option[1] : "ctrl SEMICOLON";
        try {
            inst.appendToSystemClassLoaderSearch(new JarFile(new File(new URI(jarUri))));
        } catch (IOException | URISyntaxException e_) {
            throw new RuntimeException(e_);
        }
        new SvingWindowManager(hotKey).install();
    }
}
