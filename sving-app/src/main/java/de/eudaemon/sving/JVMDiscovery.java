package de.eudaemon.sving;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class JVMDiscovery {
    static Stream<Integer> jvmPIDs() {
        ProcessBuilder pb = new ProcessBuilder("jps");
        Stream.Builder<Integer> builder = Stream.builder();
        try {
            Process jps = pb.start();
            jps.waitFor();
            BufferedReader jpsOutput = new BufferedReader(new InputStreamReader(jps.getInputStream()));
            String line = jpsOutput.readLine();
            while (line != null) {
                String[] parts = line.split("\\s+");
                if ("jps".equals(parts[1])) {
                    continue;
                }
                builder.add(Integer.parseInt(parts[0]));
                line = jpsOutput.readLine();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Couldn't run jps. Is a JDK installed?");
        }
        return builder.build();
    }
}
