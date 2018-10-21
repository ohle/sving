package de.eudaemon.sving.core;

import java.awt.*;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public class SwingHinter
        implements Hinter<Container, Component> {

    private final String hintChars;
    private final Iterator<String> hints;

    public SwingHinter(String hintChars_) {
        if (hintChars_.length() < 2) {
            throw new IllegalArgumentException("Need at least two allowed hint characters.");
        }
        hintChars = hintChars_;
        hints = new Hints();
    }

    @Override
    public Stream<Hint<? extends Component>> findHints(Container container) {
        // Needs to be split into two statements because IDEA (and older javac)'s type inference can't cope
        Stream<Hint<? extends Component>> allHints = findAllChildren(container)
                .map(comp -> Hint.create(comp, hints::next))
                .filter(Optional::isPresent)
                .map(Optional::get);
        return allHints
                .filter(h -> h.component.isShowing());
    }

    @Override
    public boolean isAllowedHintChar(char keyChar) {
        return hintChars.indexOf(keyChar) >= 0;
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
