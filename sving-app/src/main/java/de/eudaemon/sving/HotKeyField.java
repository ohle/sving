package de.eudaemon.sving;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class HotKeyField
        extends JTextField {

    HotKeyField() {
        setKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK));
        addKeyListener(new HotKeyListener());
    }

    private KeyStroke keyStroke;

    void setKeyStroke(KeyStroke keyStroke_) {
        KeyStroke old = keyStroke;
        keyStroke = keyStroke_;
        setText(keyStroke.toString());
        firePropertyChange("keyStroke", old, keyStroke);
    }

    KeyStroke getKeyStroke() {
        return keyStroke;
    }

    private class HotKeyListener
            implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            e.consume();
            int keyCode = e.getKeyCode();
            if (keyCode != KeyEvent.VK_SHIFT &&
                keyCode != KeyEvent.VK_CONTROL &&
                keyCode != KeyEvent.VK_ALT &&
                keyCode != KeyEvent.VK_ALT_GRAPH &&
                keyCode != KeyEvent.VK_META
                ) {
                setKeyStroke(KeyStroke.getKeyStrokeForEvent(e));
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            e.consume();
        }

    }
}
