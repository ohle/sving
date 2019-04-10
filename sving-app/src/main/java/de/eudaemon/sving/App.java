package de.eudaemon.sving;

import de.eudaemon.util.UnanticipatedException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class App {

    private final JFrame mainWindow;

    static void start(AgentManager agentManager)
            throws AWTException {
        new App(agentManager).installTrayIcon();
    }

    private App(AgentManager agentManager) {
        try {
            if (System.getProperty("os.name").startsWith("Linux")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e_) {
            Logger.getLogger(App.class.getName())
                    .log(Level.WARNING, "Unable to set system look and feel!");
        }
        mainWindow = new MainWindow(agentManager);
    }

    private void installTrayIcon()
            throws AWTException {
        SystemTray systemTray = SystemTray.getSystemTray();
        TrayIcon icon = new TrayIcon(loadIcon(), "Sving");
        PopupMenu menu = new PopupMenu();
        MenuItem open = new MenuItem("Open");
        MenuItem quit = new MenuItem("Quit");
        open.addActionListener(a -> showGUI());
        quit.addActionListener(a -> System.exit(0));
        menu.add(open);
        menu.add(quit);
        icon.setPopupMenu(menu);
        icon.addActionListener(a -> showGUI());
        systemTray.add(icon);
    }

    private void showGUI() {
        mainWindow.setVisible(true);
    }

    private BufferedImage loadIcon() {
        try {
            InputStream icon = App.class.getClassLoader().getResourceAsStream("icon-16.png");
            assert icon != null;
            return ImageIO.read(icon);
        } catch (IOException e_) {
            throw new UnanticipatedException(e_);
        }
    }
}
