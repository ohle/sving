package de.eudaemon.sving.core;

import java.awt.*;

public class WindowManager {
    private Window currentlyFocusedWindow;

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
        System.out.println(currentlyFocusedWindow);
    }
}
