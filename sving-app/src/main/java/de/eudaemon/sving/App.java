package de.eudaemon.sving;

import de.eudaemon.util.UnanticipatedException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

class App {

    private final JFrame mainWindow = new MainWindow();

    static void start()
            throws AWTException {
        new App().installTrayIcon();
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
            return ImageIO.read(App.class.getClassLoader().getResourceAsStream("icon-16.png"));
        } catch (IOException e_) {
            throw new UnanticipatedException(e_);
        }
    }
}
