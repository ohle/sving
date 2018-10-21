package de.eudaemon.sving.core;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;

public class SvingGlassPane
        extends JComponent
        implements HintingState.Listener<Component> {

    private final Component original;
    private Collection<Hint<Component>> visibleHints = Collections.emptySet();

    public SvingGlassPane(RootPaneContainer container_) {
        original = container_.getGlassPane();
        setFocusable(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (original != null) {
            original.repaint();
        }
        g.setColor(Color.YELLOW);
        visibleHints.forEach(hint -> {
            g.fillRect(hint.component.getX(), hint.component.getY(), 10, 10);
        });
    }

    public Component getOriginal() {
        return original;
    }

    @Override
    public void showHints(Collection<Hint<Component>> hints) {
        setVisible(true);
        visibleHints = hints;
        repaint();
    }

    @Override
    public void stopShowing() {
        setVisible(false);
        visibleHints = Collections.emptySet();
    }
}
