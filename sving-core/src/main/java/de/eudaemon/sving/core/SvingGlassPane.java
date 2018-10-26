package de.eudaemon.sving.core;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

public class SvingGlassPane
        extends JComponent
        implements HintingState.Listener<Component> {

    private final Component original;
    private Collection<Hint<? extends Component>> visibleHints = Collections.emptySet();
    private static final Logger LOG = Logger.getLogger(SvingGlassPane.class.getName());

    public SvingGlassPane(RootPaneContainer container_) {
        original = container_.getGlassPane();
        setFocusable(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        Color color = g.getColor();
        visibleHints.forEach(hint -> paintHint(g, hint));
        g.setColor(color);
    }

    private void paintHint(Graphics g, Hint<? extends Component> h) {
        LOG.finer("drawing hint '" + h.shortcut + "' for " + h.component);
        g.setColor(Solarized.BASE3);
        Point corner = SwingUtilities.convertPoint(
                h.component.getParent(),
                h.component.getX(),
                h.component.getY(),
                this
            );
        AttributedString hintText = new AttributedString(h.shortcut);
        if (h.getSuffixIndex() < h.shortcut.length()) {
            hintText.addAttribute(
                    TextAttribute.WEIGHT,
                    TextAttribute.WEIGHT_BOLD,
                    h.getSuffixIndex(),
                    h.shortcut.length()
            );
        }
        g.fillRect(corner.x, corner.y, 10, 10);
        g.setColor(Solarized.BASE02);
        g.drawString(hintText.getIterator(), corner.x, corner.y);
    }

    public Component getOriginal() {
        return original;
    }

    @Override
    public void showHints(Collection<Hint<? extends Component>> hints) {
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
