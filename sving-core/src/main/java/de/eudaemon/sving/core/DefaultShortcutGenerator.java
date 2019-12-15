package de.eudaemon.sving.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultShortcutGenerator
        implements ShortcutGenerator{

    private final String allowedCharacters;

    public DefaultShortcutGenerator(String allowedCharacters_) {
        Set<Character> chars = allowedCharacters_.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        if (allowedCharacters_.length() < 2) {
            throw new IllegalArgumentException("Need at least two distinct allowed characters.");
        }
        if (chars.size() != allowedCharacters_.length()) {
            throw new IllegalArgumentException("Allowed characters should be distinct.");
        }
        allowedCharacters = allowedCharacters_;
    }

    @Override
    public boolean isAllowedHintChar(char keyChar_) {
        return allowedCharacters.indexOf(keyChar_) >= 0;
    }

    @Override
    public List<String> generate(int size_) {
        LinkedList<String> generated = new LinkedList<>();
        generated.push("");
        while (generated.size() < size_) {
            String currentPrefix = generated.pop();
            int runLength = Math.min(allowedCharacters.length(), size_ - generated.size());
            for (int i=0; i < runLength; i++) {
                generated.add(currentPrefix + allowedCharacters.charAt(i));
            }
        }
        return generated;
    }
}
