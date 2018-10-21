package de.eudaemon.sving.core;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

class Hint<C> {
    final C component;
    final String shortcut;
    private final Consumer<C> action;
    private int inactivePrefixLength;


    static Optional<Hint<? extends Component>> create(Component component, String shortcut) {
        return create(component, () -> shortcut);
    }

    static Optional<Hint<? extends Component>> create(Component component, Supplier<String> shortcut) {
        if (component instanceof AbstractButton) {
            return Optional.of(new Hint<>((AbstractButton) component, shortcut.get(), AbstractButton::doClick));
        } else if (component instanceof JTextComponent) {
            return Optional.of(new Hint<>((JTextComponent) component, shortcut.get(), JTextComponent::requestFocus));
        }
        return Optional.empty();
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

    Hint afterNextKey() {
        Hint<C> next = new Hint<>(this);
        next.inactivePrefixLength = inactivePrefixLength + 1;
        return next;
    }

    @Override
    public String toString() {
        return "Hint{" +
                "component=" + component +
                ", shortcut='" + shortcut + '\'' +
                ", inactivePrefixLength=" + inactivePrefixLength +
                '}';
    }
}
