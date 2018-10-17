package de.eudaemon.sving.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class  WindowManager {

    private RootPaneContainer currentlyFocusedWindow = null;
    private SvingGlassPane installedGlassPane = null;

    private static final Logger LOG = Logger.getLogger(WindowManager.class.getName());

    public WindowManager() {
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
                if (isHotkey(e) && e.getID() == KeyEvent.KEY_RELEASED) {
                    System.out.println("WindowManager.dispatchKeyEvent");
                    return true;
                }
                return false;
            }

            private boolean isHotkey(KeyEvent e) {
                return ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) && e.getKeyChar() == ';';
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
        LOG.fine("Installed GlassPane on " + newWindow);
    }
}
