package de.eudaemon.sving.core;

import co.unruly.matchers.StreamMatchers;
import de.eudaemon.sving.core.testapp.TestWindow;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.stream.Stream;

import static co.unruly.matchers.StreamMatchers.anyMatch;
import static co.unruly.matchers.StreamMatchers.empty;
import static de.eudaemon.sving.core.Matchers.hasComponentWithName;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class HinterTest {
    private final Hinter hinter = new Hinter();

    private static FrameFixture window;

    @BeforeAll
    public static void setup() {
        JFrame frame = GuiActionRunner.execute(TestWindow::new);
        window = new FrameFixture(frame);
        window.show();
    }

    @Test
    void findsComponents() {
        window.tabbedPane().selectTab(0);
        assertThat(hinter.findHints(window.target()), anyMatch(hasComponentWithName(equalTo("checkbox"))));
        assertThat(hinter.findHints(window.target()), anyMatch(hasComponentWithName(equalTo("radio-button"))));
        assertThat(hinter.findHints(window.target()), anyMatch(hasComponentWithName(equalTo("test-button"))));
        assertThat(hinter.findHints(window.target()), anyMatch(hasComponentWithName(equalTo("dialog-button"))));
        assertThat(hinter.findHints(window.target()), anyMatch(hasComponentWithName(equalTo("input"))));
    }

    @Test
    void skipsIrrelevantComponents() {
        window.tabbedPane().selectTab(0);
        Stream<Hint> hints = hinter.findHints(window.target());
        assertThat(hints, not(anyMatch(hasComponentWithName(equalTo("label")))));
    }

    @Test
    void skipsInvisibleComponents() {
        window.tabbedPane().selectTab(1);
        assertThat(hinter.findHints(window.target()), empty());
    }

}
