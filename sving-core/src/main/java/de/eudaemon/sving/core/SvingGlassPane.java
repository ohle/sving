package de.eudaemon.sving.core;

import javax.swing.*;
import java.awt.*;

public class SvingGlassPane extends JComponent {
    private final Component original;
    private final Container container;

    public SvingGlassPane(RootPaneContainer container_) {
        original = container_.getGlassPane();
        container = (Container) container_;
        setFocusable(true);
    }

    @Override
    public void paint(Graphics g) {
        original.paint(g);
    }

    public Component getOriginal() {
        return original;
    }
}
