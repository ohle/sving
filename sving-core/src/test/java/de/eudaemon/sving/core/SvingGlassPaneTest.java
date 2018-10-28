package de.eudaemon.sving.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith(MockitoExtension.class)
class SvingGlassPaneTest {
    @Mock RootPaneContainer container;
    @Mock Window root;
    @InjectMocks  private SvingGlassPane pane;

    @Mock Graphics2D graphics2D;

    Hint<? extends Component> hintA;
    Hint<? extends Component> hintB;
    Hint<? extends Component> hintC;

    @BeforeEach
    void setup() {
        JButton button = mock(JButton.class);
        when(button.getParent()).thenReturn(root);
        when(root.getLocationOnScreen()).thenReturn(new Point(0, 0));
        setupG2D();
        hintA = Hint.create(button, "a").get();
        hintB = Hint.create(button, "b").get();
        hintC = Hint.create(button, "c").get();
    }

    private void setupG2D() {
        FontRenderContext frc = mock(FontRenderContext.class, RETURNS_DEEP_STUBS);
        when(graphics2D.getFontRenderContext()).thenReturn(frc);
    }

    @Test
    void drawsCorrectHints() {
        pane.showHints(List.of(hintA));
        pane.paintComponent(graphics2D);
        verify(graphics2D).drawString(argThat(printsAs(equalTo("a"))), anyFloat(), anyFloat());
        verify(graphics2D, times(1)).drawString(any(AttributedCharacterIterator.class), anyFloat(), anyFloat());
    }

    @Test
    void onlyDrawsCurrentHints() {
        pane.showHints(List.of(hintA));
        pane.paintComponent(graphics2D);
        verify(graphics2D).drawString(argThat(printsAs(equalTo("a"))), anyFloat(), anyFloat());
        pane.showHints((List.of(hintB)));
        pane.paintComponent(graphics2D);
        verify(graphics2D).drawString(argThat(printsAs(equalTo("b"))), anyFloat(), anyFloat());
        verify(graphics2D, times(2)).drawString(any(AttributedCharacterIterator.class), anyFloat(), anyFloat());
    }

    private ACIMatcher printsAs(Matcher<String> m) {
        return new ACIMatcher(m);
    }

    class ACIMatcher
            extends TypeSafeDiagnosingMatcher<AttributedCharacterIterator> {

        private final Matcher<String> s;

        ACIMatcher(Matcher<String> s_) {
            s = s_;
        }

        @Override
        protected boolean matchesSafely(AttributedCharacterIterator item, Description mismatchDescription) {
            StringBuilder b = new StringBuilder();
            for (char c = item.first(); c != CharacterIterator.DONE; c = item.next()) {
                b.append(c);
            }
            if (!s.matches(b.toString())) {
                s.describeMismatch(b.toString(), mismatchDescription);
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("An AttributedCharacterIterator over a String ");
            s.describeTo(description);
        }
    }
}
