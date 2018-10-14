package de.eudaemon.sving;

import java.util.*;

public class CLI {
    public static void main(String[] args_) {
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

    private static void runJar(String jarFile, ArrayList<String> jarArguments) {
        System.out.printf(
                "Running %s with %s…\n",
                jarFile,
                jarArguments.isEmpty() ? "no arguments" : "the arguments " + String.join(" " , jarArguments)
                );
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
