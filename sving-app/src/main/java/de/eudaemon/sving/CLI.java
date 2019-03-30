package de.eudaemon.sving;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import de.eudaemon.sving.core.manager.SvingWindowManager;
import de.eudaemon.util.UnanticipatedException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CLI {
    private static Logger log = Logger.getLogger(CLI.class.getName());

    private static Logger rootLogger = Logger.getLogger("de.eudaemon.sving");

    private static final AgentManager agentManager = new AgentManager(findAgentJar());

    public static void main(String... args_) {
        Queue<String> args = new LinkedList<>(Arrays.asList(args_));
        Level logLevel = Level.INFO;
        Runnable action = CLI::startApp;
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
                case "--attach":
                case "-a":
                    if (args.isEmpty()) {
                        System.err.println("Missing PID argument");
                        help();
                        System.exit(1);
                    }
                    final String pid = args.remove();
                    action = () -> attachAgent(pid);
                    break;
                case "--jar":
                    if (args.isEmpty()) {
                        System.err.println("Missing jarFile argument");
                        help();
                        System.exit(1);
                    }
                    final String jarFile = args.remove();
                    final ArrayList<String> jarArguments = slurpRemaining(args);
                    action = () -> runJar(jarFile, jarArguments);
                    break;
                case "--main-class":
                    if (args.isEmpty()) {
                        System.err.println("Missing class name argument");
                        help();
                        System.exit(1);
                    }
                    String className = args.remove();
                    ArrayList<String> arguments = slurpRemaining(args);
                    action = () -> runClass(className, arguments);
                case "-v":
                    logLevel = Level.FINE;
                    break;
                case "-vv":
                    logLevel = Level.FINER;
                    break;
                default:
                    System.err.println("Unknown argument: " + argument);
                    help();
                    System.exit(1);
            }
        }
        setupLogging(logLevel);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> rootLogger.log(Level.SEVERE, "Uncaught Exception", e));
        action.run();
    }

    private static void startApp() {
        try {
            App.start(agentManager);
        } catch (AWTException e_) {
            sneakyThrow(e_);
        }
    }

    private static ArrayList<String> slurpRemaining(Queue<String> args_) {
        ArrayList<String> jarArguments = new ArrayList<>();
        while (!args_.isEmpty()) {
            jarArguments.add(args_.remove());
        }
        return jarArguments;
    }

    private static void setupLogging(Level logLevel) {
        SystemOutHandler handler = new SystemOutHandler();
        handler.init();
        handler.setLevel(logLevel);
        rootLogger.setLevel(logLevel);
        rootLogger.addHandler(handler);
        SimpleFormatter formatter = new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                String output = String.format("[%s] %s\n", record.getLevel(), record.getMessage());
                if (record.getThrown() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    output = output + sw.toString();
                }
                return output;
            }
        };
        handler.setFormatter(formatter);
    }

    private static void runJar(String jarFile, ArrayList<String> jarArguments) {
        log.info(String.format(
                "Running %s with %s…\n",
                jarFile,
                jarArguments.isEmpty() ? "no arguments" : "the arguments " + String.join(" " , jarArguments)
                ));
        try {
            URLClassLoader loader = new URLClassLoader(new URL[]{ Paths.get(jarFile).toUri().toURL() }, CLI.class.getClassLoader());
            JarFile jar = new JarFile(jarFile);
            String mainClassName = jar.getManifest().getMainAttributes().getValue("Main-Class");
            runMainClassAndInstall(mainClassName, jarArguments, loader);
        } catch (MalformedURLException | ClassNotFoundException e) {
            throw new UnanticipatedException(e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error loading " + jarFile + ": " + e.getMessage());
        }
    }

    private static void runClass(String className, ArrayList<String> arguments) {
        try {
            runMainClassAndInstall(className, arguments, CLI.class.getClassLoader());
        } catch (ClassNotFoundException e_) {
            log.log(Level.SEVERE, "Could not find main class: " + e_.getMessage());
        }
    }

    private static void attachAgent(String pid) {
        Optional<VirtualMachineDescriptor> descriptor = VirtualMachine.list().stream()
                .filter(d -> d.id().equals(pid))
                .findFirst();
        if (descriptor.isPresent()) {
            agentManager.attachTo(descriptor.get());
        } else {
            log.log(Level.SEVERE, "No JVM found with PID " + pid + "!");
        }
    }

    private static File findAgentJar() {
        try {
            String jarPath = System.getenv("SVING_AGENT_JAR");
            if (jarPath != null) {
                return new File(jarPath);
            } else {
                File path = new File(CLI.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
                return new File(path, "sving-agent.jar");
            }
        } catch (URISyntaxException e_) {
            throw new UnanticipatedException(e_);
        }
    }

    private static void runMainClassAndInstall(
            String mainClassName_,
            Collection<String> jarArguments,
            ClassLoader loader_
            ) throws ClassNotFoundException {
        try {
            Method main = loader_.loadClass(mainClassName_).getMethod("main", String[].class);
            main.invoke(null, (Object) jarArguments.toArray(new String[]{}));
            new SvingWindowManager().install();
        } catch (NoSuchMethodException | IllegalAccessException e_) {
            e_.printStackTrace();
        } catch (InvocationTargetException e_) {
            sneakyThrow(e_.getCause()); // Fail like the invoked application would
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    private static void help() {
        System.out.println("Usage:");
        System.out.println("  sving [OPTIONS] (-d | --daemon)");
        System.out.println("      Start sving in daemon mode (attaches to any running JVMs and displays a");
        System.out.println("      tray icon for configuration)");
        System.out.println("  sving [OPTIONS] --jar <jarFile> [arguments...]");
        System.out.println("      Run sving with the given executable jar");
        System.out.println("  sving [OPTIONS] --main-class <className> [arguments...]");
        System.out.println("      Run sving with the given main class (which must be on the classpath)");
        System.out.println("  sving [OPTIONS] (-a | --attach) <pid>");
        System.out.println("      Attach the sving agent to the given JVM process");
        System.out.println();
        System.out.println(" OPTIONS");
        System.out.println(" -v  Verbose output");
        System.out.println(" -vv Very verbose output");
    }
}
