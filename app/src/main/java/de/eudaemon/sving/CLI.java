package de.eudaemon.sving;

import de.eudaemon.util.UnanticipatedException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CLI {
    private static Logger log = Logger.getLogger(CLI.class.getName());

    public static void main(String[] args_)
            throws Throwable {
        Queue<String> args = new LinkedList<>(Arrays.asList(args_));
        while (!args.isEmpty()) {
            String argument = args.remove();
            switch(argument) {
                case "--help":
                case "-h":
                    help();
                    break;
                case "--daemon":
                case "-d":
                    throw new UnsupportedOperationException("Not implemented!");
                case "--jar":
                    if (args.isEmpty()) {
                        System.err.println("Missing jarFile argument");
                        help();
                        System.exit(1);
                    }
                    String jarFile = args.remove();
                    ArrayList<String> jarArguments = new ArrayList<>();
                    while (!args.isEmpty()) {
                        jarArguments.add(args.remove());
                    }
                    runJar(jarFile, jarArguments);
                    break;
                default:
                    System.err.println("Unknown argument: " + argument);
                    help();
                    System.exit(1);
            }
        }
    }

    private static void runJar(String jarFile, ArrayList<String> jarArguments)
            throws Throwable {
        log.info(String.format(
                "Running %s with %s…\n",
                jarFile,
                jarArguments.isEmpty() ? "no arguments" : "the arguments " + String.join(" " , jarArguments)
                ));
        try {
            URLClassLoader loader = new URLClassLoader(new URL[]{ Paths.get(jarFile).toUri().toURL() }, CLI.class.getClassLoader());
            JarFile jar = new JarFile(jarFile);
            String mainClassName = jar.getManifest().getMainAttributes().getValue("Main-Class");
            Method main = loader.loadClass(mainClassName).getMethod("main", String[].class);
            main.invoke(null, (Object) jarArguments.toArray(new String[]{}));
        } catch (MalformedURLException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new UnanticipatedException(e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error loading " + jarFile, e);
        } catch (InvocationTargetException e_) {
            throw e_.getCause();
        }
    }

    private static void help() {
        System.out.println("Usage:");
        System.out.println("  sving (-d | --daemon)");
        System.out.println("      Start sving in daemon mode (attaches to any running JVMs and displays a");
        System.out.println("      tray icon for configuration)");
        System.out.println("  sving --jar <jarFile>");
        System.out.println("      Run sving with the given executable jar");
    }
}
