package de.eudaemon.sving.core.manager;

import de.eudaemon.sving.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The SvingWindowManager listens for its configured hotkey and displays
 * hints on the visible components of the currently focused window
 */
public class SvingWindowManager {

    private RootPaneContainer currentlyFocusedWindow = null;
    private SvingGlassPane installedGlassPane = null;
    private final Hinter<Container, Component> hinter = new SwingHinter(new DefaultShortcutGenerator("abc"));
    private HintingState<Container, Component> hintingState = null;
    private KeyStroke hotKey;

    private static final Logger LOG = Logger.getLogger(SvingWindowManager.class.getName());

    /**
     * Installs the WindowManager in the current VM
     * @param hotKey_
     */
    public void install(String hotKey_) {
        hotKey = KeyStroke.getKeyStroke(hotKey_);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                "focusedWindow",
                e -> updateWindow()
                );
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                "managingFocus",
                e -> updateWindow()
                );
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (hintingState == null) {
                    return false;
                }
                if (isHotkey(e)) {
                    LOG.fine("Hotkey received");
                    hintingState.hotkeyPressed();
                    return true;
                }
                if (e.getID() != KeyEvent.KEY_RELEASED) {
                    return false;
                }
                if (hinter.isAllowedHintChar(e.getKeyChar())) {
                    LOG.finer("Hint refinement: " + e.getKeyChar());
                    hintingState.keyPressed(e.getKeyChar());
                    return true;
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    LOG.fine("Hinting aborted");
                    hintingState.escapePressed();
                    return true;
                }
                return false;
            }

            private boolean isHotkey(KeyEvent e) {
                return KeyStroke.getKeyStrokeForEvent(e).equals(hotKey);
            }
        });
        updateWindow();
    }

    private void updateWindow() {
        RootPaneContainer oldWindow = currentlyFocusedWindow;
        Window focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        if (focusedWindow instanceof RootPaneContainer) {
            currentlyFocusedWindow = (RootPaneContainer) focusedWindow;
        }
        LOG.log(Level.FINE, "Focus switched to " + (focusedWindow == null ? "None" : focusedWindow.getName()));
        if (currentlyFocusedWindow != null && !currentlyFocusedWindow.equals(oldWindow)) {
            installGlassPane(oldWindow, currentlyFocusedWindow);
        }
    }

    private void installGlassPane(
            RootPaneContainer oldWindow,
            RootPaneContainer newWindow
            ) {
        if (oldWindow != null) {
            oldWindow.setGlassPane(installedGlassPane.getOriginal());
        }
        installedGlassPane = new SvingGlassPane(currentlyFocusedWindow);
        newWindow.setGlassPane(installedGlassPane);
        HintingState<Container, Component> hintingState = new HintingState<>(hinter, newWindow.getContentPane());
        hintingState.addListener(installedGlassPane);
        this.hintingState = hintingState;
        LOG.fine("Installed GlassPane on " + newWindow);
    }
}
