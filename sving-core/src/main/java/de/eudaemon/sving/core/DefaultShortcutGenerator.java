package de.eudaemon.sving.core;

import java.util.ArrayList;
import java.util.List;

public class DefaultShortcutGenerator
        implements ShortcutGenerator{

    private final String allowedCharacters;

    public DefaultShortcutGenerator(String allowedCharacters_) {
        allowedCharacters = allowedCharacters_;
    }

    @Override
    public boolean isAllowedHintChar(char keyChar_) {
        return allowedCharacters.indexOf(keyChar_) >= 0;
    }

    @Override
    public List<String> generate(int size_) {
        List<String> dummy = new ArrayList<>();
        for (int i=0; i<size_; i++) {
            dummy.add(i, "");
        }
        return dummy;
    }
}
