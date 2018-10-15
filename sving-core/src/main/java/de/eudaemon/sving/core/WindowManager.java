package de.eudaemon.sving.core;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WindowManager {

    private Window currentlyFocusedWindow;
    private static final Logger log = Logger.getLogger(WindowManager.class.getName());

    public WindowManager() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                "focusedWindow",
                e -> updateWindow()
                );
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                "managingFocus",
                e -> updateWindow()
                );
        updateWindow();
    }

    private void updateWindow() {
        currentlyFocusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        log.log(Level.FINE, "Focus switched to " + (currentlyFocusedWindow == null ? "None" : currentlyFocusedWindow.getName()));
    }
}
