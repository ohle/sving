package de.eudaemon.sving;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.Test;

public class End2End {

    @Test
    void startsMainClass() {
        String mainClassName = "de.eudaemon.sving.testapp.TestApp";
        CLI.main("--main-class", mainClassName);
        FrameFixture mainFrame = WindowFinder.findFrame("test-app-frame").using(BasicRobot.robotWithCurrentAwtHierarchy());
        mainFrame.requireVisible();
    }
}
