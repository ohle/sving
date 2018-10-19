package de.eudaemon.sving.core;

import java.awt.*;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public class Hinter {

    private final String hintChars;
    private final Iterator<String> hints;

    public Hinter(String hintChars_) {
        if (hintChars_.length() < 2) {
            throw new IllegalArgumentException("Need at least two allowed hint characters.");
        }
        hintChars = hintChars_;
        hints = new Hints();
    }

    public Stream<Hint> findHints(Container container) {
        return findAllChildren(container)
                .map(comp -> Hint.create(comp, hints::next))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(h -> h.component.isShowing());
    }

    private Stream<Component> findAllChildren(Container container) {
        Stream.Builder<Component> builder = Stream.builder();
        addAllChildren(container, builder);
        return builder.build();
    }

    private void addAllChildren(Component component, Stream.Builder<Component> builder) {
        builder.add(component);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                addAllChildren(child, builder);
            }
        }
    }

    private class Hints
            implements Iterator<String> {

        private int idx = 0;
        private final int base = hintChars.length();

        @Override
        public String next() {
            StringBuilder hint = new StringBuilder();
            int n = idx;
            do {
                hint.append(hintChars.charAt(n % base));
                n /= base;
            } while (n != 0);
            idx++;
            return hint.toString();
        }

        @Override
        public boolean hasNext() {
            return true;
        }
    }
}
