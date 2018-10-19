package de.eudaemon.sving.core;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Hint<C extends Component> {
    public final C component;
    public final String shortcut;
    private final Consumer<C> action;


    public static Optional<Hint> create(Component component, String shortcut) {
        return create(component, () -> shortcut);
    }

    public static Optional<Hint> create(Component component, Supplier<String> shortcut) {
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
    }

    public void execute() {
        action.accept(component);
    }
}
