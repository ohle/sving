package de.eudaemon.sving.core;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

class Hint<C> {
    final C component;
    final String shortcut;
    private final Consumer<C> action;
    private int inactivePrefixLength;

    private static class Action<COMP> {
        Class<COMP> clazz;
        Consumer<COMP> action;
        Action(Class<COMP> clazz_, Consumer<COMP> action_) {
            clazz = clazz_;
            action = action_;
        }

        boolean supports(Object component) {
            return clazz.isInstance(component);
        }
    }

    private static final Set<Action> SUPPORTED_ACTIONS = new LinkedHashSet<>();
    static {
        SUPPORTED_ACTIONS.add(new Action<>(AbstractButton.class, AbstractButton::doClick));
        SUPPORTED_ACTIONS.add(new Action<>(JTextComponent.class, JTextComponent::requestFocusInWindow));
    }

    static Optional<Hint<? extends Component>> create(Component component, String shortcut) {
        return create(component, () -> shortcut);
    }

    @SuppressWarnings("unchecked")
    static Optional<Hint<? extends Component>> create(Component component, Supplier<String> shortcut) {
        return SUPPORTED_ACTIONS.stream()
                .filter(a -> a.supports(component))
                .findFirst()
                .map(a -> new Hint<>(component, shortcut.get(), a.action));
    }

    private Hint(C component_, String shortcut_, Consumer<C> action_) {
        component = component_;
        shortcut = shortcut_;
        action = action_;
        inactivePrefixLength = 0;
    }

    private Hint(Hint<C> hint) {
        component = hint.component;
        shortcut = hint.shortcut;
        action = hint.action;
        inactivePrefixLength = hint.inactivePrefixLength;
    }

    void execute() {
        action.accept(component);
    }

    String getInactivePrefix() {
        return shortcut.substring(0, inactivePrefixLength);
    }

    String getActiveSuffix() {
        return shortcut.substring(inactivePrefixLength);
    }

    int getSuffixIndex() {
        return inactivePrefixLength;
    }

    Hint<C> afterNextKey() {
        Hint<C> next = new Hint<>(this);
        next.inactivePrefixLength = inactivePrefixLength + 1;
        return next;
    }

    @Override
    public String toString() {
        return "Hint{" +
                "component=" + component.getClass().getSimpleName() +
                ", shortcut='" + shortcut + '\'' +
                ", inactivePrefixLength=" + inactivePrefixLength +
                '}';
    }

    @Override
    public boolean equals(Object o_) {
        if (this == o_) return true;
        if (o_ == null || getClass() != o_.getClass()) return false;
        Hint<?> hint = (Hint<?>) o_;
        return inactivePrefixLength == hint.inactivePrefixLength &&
                Objects.equals(component, hint.component) &&
                Objects.equals(shortcut, hint.shortcut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, shortcut, inactivePrefixLength);
    }
}
