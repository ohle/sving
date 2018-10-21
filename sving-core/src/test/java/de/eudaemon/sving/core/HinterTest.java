package de.eudaemon.sving.core;

import de.eudaemon.sving.core.testapp.TestWindow;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JCheckBoxFixture;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.eudaemon.sving.core.Matchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

public class HinterTest {
    private final SwingHinter hinter = new SwingHinter("abc");

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
        Stream<Hint<? extends Component>> hints = hinter.findHints(window.target());
        assertThat(hints, not(anyMatch(hasComponentWithName(equalTo("label")))));
    }

    @Test
    void skipsInvisibleComponents() {
        window.tabbedPane().selectTab(1);
        assertThat(hinter.findHints(window.target()).collect(Collectors.toList()), empty());
    }

    @Test
    void togglesCheckbox() {
        JCheckBoxFixture checkbox = window.checkBox("checkbox");
        checkbox.uncheck();
        findHint("checkbox").execute();
        checkbox.requireSelected();
    }

    @Test
    void pressesButton() {
        SwingUtilities.invokeLater(() -> findHint("dialog-button").execute() );
        DialogFixture dialog = WindowFinder.findDialog(JDialog.class).withTimeout(1000).using(window.robot());
        dialog.requireVisible();
        dialog.button(new GenericTypeMatcher<>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return "OK".equals(button.getText());
            }
        }).click();
    }

    @Test
    void selectsRadioButton() {
        findHint("radio-button").execute();
        window.radioButton().requireSelected();
    }

    @Test
    void constructsMinimalHintsFromCharacters() {
        assertThat(
                hinter.findHints(window.target()).map(h -> h.shortcut).collect(Collectors.toSet()),
                hasItems("a", "b", "c", "ab")
                );
    }

    private Hint findHint(String name) {
        return hinter.findHints(window.target())
                .filter(h -> h.component.getName().equals(name))
                .findFirst().get();
    }
}