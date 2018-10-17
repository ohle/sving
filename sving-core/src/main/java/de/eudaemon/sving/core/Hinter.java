package de.eudaemon.sving.core;

import java.awt.*;
import java.util.stream.Stream;

public class Hinter {
    public Stream<Hint> findHints(Container container) {
        return findAllChildren(container).map(comp -> new Hint<>(comp, "abc"));
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

}
