package de.eudaemon.sving.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

public class SvingGlassPane
        extends JComponent
        implements HintingState.Listener<Component>,
        FocusListener {

    private final Component original;
    private Collection<Hint<? extends Component>> visibleHints = Collections.emptySet();
    private static final Logger LOG = Logger.getLogger(SvingGlassPane.class.getName());

    public SvingGlassPane(RootPaneContainer container_) {
        original = container_.getGlassPane();
        setFocusable(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Color color = g.getColor();
        visibleHints.forEach(hint -> paintHint(g2d, hint));
        g.setColor(color);
    }

    private void paintHint(Graphics2D g, Hint<? extends Component> h) {
        LOG.finer("drawing hint '" + h.shortcut + "' for " + h.component);
        g.setColor(Solarized.BASE3);
        Point corner = SwingUtilities.convertPoint(
                h.component.getParent(),
                h.component.getX(),
                h.component.getY(),
                this
            );
        AttributedString hintText = highlightShortcut(h);
        TextLayout layout = new TextLayout(hintText.getIterator(), g.getFontRenderContext());
        final int padding = 2;
        final int height = (int) (layout.getAscent() +  2 * padding);
        final int width = (int) (layout.getAdvance() + 2 * padding);
        g.fillRect(corner.x, corner.y, width, height);
        g.setColor(Solarized.BASE02);
        g.drawRect(corner.x, corner.y, width, height);
        g.drawString(hintText.getIterator(), corner.x + padding, corner.y + layout.getAscent() + padding);
    }

    private AttributedString highlightShortcut(Hint<? extends Component> h) {
        AttributedString hintText = new AttributedString(h.shortcut);
        if (h.getSuffixIndex() < h.shortcut.length()) {
            hintText.addAttribute(
                    TextAttribute.WEIGHT,
                    TextAttribute.WEIGHT_BOLD,
                    h.getSuffixIndex(),
                    h.shortcut.length()
            );
        }
        return hintText;
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

    @Override
    public void setVisible(boolean v) {
        if (v) {
            requestFocus();
        }
        super.setVisible(v);
    }

    @Override
    public void focusGained(FocusEvent e) {
        // good!
    }

    @Override
    public void focusLost(FocusEvent e) {
        // hold on to focus
        if (isVisible()) {
            requestFocus();
        }
    }
}
