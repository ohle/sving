package de.eudaemon.sving.core;

import java.util.stream.Stream;

public interface Hinter<T> {
    Stream<Hint> findHints(T container);
}
