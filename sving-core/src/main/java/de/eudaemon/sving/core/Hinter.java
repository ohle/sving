package de.eudaemon.sving.core;

import java.util.stream.Stream;

public interface Hinter<CONTAINER, COMPONENT> {
    Stream<Hint<? extends COMPONENT>> findHints(CONTAINER container);

    boolean isAllowedHintChar(char keyChar_);
}
