package de.eudaemon.sving.core;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SwingHinter
        implements Hinter<Container, Component> {

    private final ShortcutGenerator shortcutGenerator;

    public SwingHinter(ShortcutGenerator shortcuts_) {
        shortcutGenerator = shortcuts_;
    }

    @Override
    public Stream<Hint<? extends Component>> findHints(Container container) {
        List<? extends Component> eligibleComponents = findAllChildren(container)
                .filter(Hint::supportsComponent)
                .filter(Component::isShowing)
                .collect(Collectors.toList());
        Iterator<String> shortcuts = shortcutGenerator.generate(eligibleComponents.size()).iterator();
        return eligibleComponents.stream()
                .map(c -> Hint.create(c, shortcuts.next()))
                .map(Optional::get);
    }

    @Override
    public boolean isAllowedHintChar(char keyChar) {
        return shortcutGenerator.isAllowedHintChar(keyChar);
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
