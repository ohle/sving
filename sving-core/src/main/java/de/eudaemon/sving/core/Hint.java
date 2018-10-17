package de.eudaemon.sving.core;

import java.awt.*;

public class Hint<C extends Component> {
    public final C component;
    public final String shortcut;

    public Hint(C component_, String shortcut_) {
        component = component_;
        shortcut = shortcut_;
    }
}
