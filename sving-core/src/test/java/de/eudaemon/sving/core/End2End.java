package de.eudaemon.sving.core;

import de.eudaemon.sving.core.manager.SvingWindowManager;
import de.eudaemon.sving.core.testapp.TestWindow;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_SEMICOLON;

public class End2End {

    private static FrameFixture window;

    @BeforeAll
    static void setup() {
        JFrame frame = GuiActionRunner.execute(TestWindow::new);
        window = new FrameFixture(frame);
        window.show();
        new SvingWindowManager().install();
    }

    @AfterAll
    static void cleanup() {
        window.cleanUp();
    }

    @Test
    void clicksButton() throws InterruptedException {
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
}
