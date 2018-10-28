package de.eudaemon.sving.core;

import java.util.List;

public interface ShortcutGenerator {
    boolean isAllowedHintChar(char keyChar_);
    List<String> generate(int size_);
}
