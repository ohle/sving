package de.eudaemon.sving;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

enum Icon {
    ATTACHED("attached"), STAR("star"), NO_STAR("non-starred");

    private final String name;
    private Map<Integer, ImageIcon> bySize = new HashMap<>();

    Icon(String name_) {
        name = name_;
    }
    private static final Logger LOG = Logger.getLogger(Icon.class.getName());


    public ImageIcon get(int size) {
        return bySize.computeIfAbsent(size, this::loadIcon);
    }

    private ImageIcon loadIcon(int size) {
        InputStream imageStream = MainWindow.class.getClassLoader().getResourceAsStream(String.format("%s-%d.png", name, size));
        assert imageStream != null;
        try {
            return new ImageIcon(ImageIO.read(imageStream));
        } catch (IOException e_) {
            LOG.log(Level.WARNING, "Couldn't load icon '" + name + "'");
            return new ImageIcon();
        }
    }
}
