package de.eudaemon.sving.core;

import de.eudaemon.sving.core.manager.SvingWindowManager;
import de.eudaemon.sving.core.testapp.TestWindow;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.KeyEvent;

import static java.awt.event.InputEvent.getModifiersExText;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_SEMICOLON;

public class End2End {

    private FrameFixture window;

    @BeforeEach
    void setup() {
        JFrame frame = GuiActionRunner.execute(TestWindow::new);
        window = new FrameFixture(frame);
        window.show();
    }

    @AfterEach
    void cleanup() {
        window.cleanUp();
    }

    @Test
    void clicksButton() throws InterruptedException {
        new SvingWindowManager("ctrl SEMICOLON").install();
        SwingHinter hinter = new SwingHinter(new DefaultShortcutGenerator("abc"));
        JButton dialogButton = window.button("dialog-button").target();
        String shortCut = hinter.findHints(window.target())
                .filter(h -> h.component == dialogButton)
                .map(h -> h.shortcut)
                .findAny().get();
        window.pressKey(VK_CONTROL).pressAndReleaseKeys(VK_SEMICOLON).releaseKey(VK_CONTROL);
        Thread.sleep(1000);
        shortCut.chars()
                .map(KeyEvent::getExtendedKeyCodeForChar)
                .forEach(window::pressAndReleaseKeys);
        JOptionPaneFinder.findOptionPane().using(window.robot()).requireVisible();
    }

    @Test
    void hintKeyPressesDoNotLeak() throws InterruptedException {
        DefaultShortcutGenerator shortcuts = new DefaultShortcutGenerator("ab");
        SwingHinter hinter = new SwingHinter(shortcuts);
        new SvingWindowManager("ctrl SEMICOLON", shortcuts).install();
        JTextComponent target = window.textBox().target();
        String shortCut = hinter.findHints(window.target())
                .peek(System.out::println)
                .filter(h -> h.component == target)
                .map(h -> h.shortcut)
                .findAny().get();
        System.out.println(shortCut);
        window.pressKey(VK_CONTROL).pressAndReleaseKeys(VK_SEMICOLON).releaseKey(VK_CONTROL);
        Thread.sleep(200);
        shortCut.chars()
                .map(KeyEvent::getExtendedKeyCodeForChar)
                .forEach(window::pressAndReleaseKeys);
        window.textBox().requireFocused();
        window.textBox().requireEmpty();
    }
}
