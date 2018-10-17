package de.eudaemon.sving.testapp;

import de.eudaemon.sving.core.testapp.TestWindow;

import javax.swing.*;
import java.awt.*;

public class TestApp {

    private final JFrame mainWindow;

    public TestApp() {
        mainWindow = new TestWindow();
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        EventQueue.invokeLater(() -> {
            mainWindow.pack();
            mainWindow.setVisible(true);
        });
    }

    public static void main(String[] args) {
        TestApp app = new TestApp();
        app.start();
    }

    private void start() {

    }
}
