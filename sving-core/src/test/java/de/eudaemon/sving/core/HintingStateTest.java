package de.eudaemon.sving.core;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;


@ExtendWith(MockitoExtension.class)
class HintingStateTest {

    @Mock JButton component;
    private Hint hintA;
    private Hint hintAB;
    private Hint hintB;

    @Mock Hinter<Object> hinter;

    @Mock HintingState.Listener listener;

    private HintingState<Object> state;

    @BeforeEach
    void setup() {
        hintA  = Hint.create(component, "a").get();
        hintAB = Hint.create(component, "ab").get();
        hintB  = Hint.create(component, "b").get();
        List<Hint> hintsA = List.of(hintA, hintAB, hintB);
        List<Hint> hintsB = List.of(Hint.create(component, "foo").get());

        state = new HintingState<>(hinter, null);

        lenient().when(hinter.findHints(any()))
                .thenReturn(hintsA.stream())
                .thenReturn(hintsB.stream());
    }

    @Test
    @SuppressWarnings("unchecked")
    void firesHintingActivatedWhenHotkeyPressedAndThereAreHints() {
        state.addListener(listener);
        state.hotkeyPressed();
        verify(listener).showHints((Collection<Hint>)argThat(
                containsInAnyOrder(
                        hintA, hintB, hintAB
                )
        ));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildsUpPrefix() {
        state.addListener(listener);
        state.hotkeyPressed();
        state.keyPressed('a');
        verify(listener).showHints((List<Hint>)argThat(
                containsInAnyOrder(
                        hintWithPrefix("a"),
                        hintWithPrefix("a")
                )));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildsUpPrefixFurther() {
        state.addListener(listener);
        state.hotkeyPressed();
        state.keyPressed('a');
        state.keyPressed('b');
        verify(listener).showHints((List<Hint>)argThat(
                contains(
                        hintWithPrefix("ab")
                )));
    }

    @Test
    void cancelsHintingWhenNoHintsLeft() {
        state.addListener(listener);
        state.hotkeyPressed();
        state.keyPressed('q');
        verify(listener).stopShowing();
    }

    @Test
    void propagatesUserCancelEvent() {
        state.addListener(listener);
        state.escapePressed();
        verify(listener).stopShowing();
    }

    private static HintWithPrefix hintWithPrefix(String prefix) {
        return new HintWithPrefix(prefix);
    }

    private static final class HintWithPrefix
            extends TypeSafeDiagnosingMatcher<Hint> {

        private final String prefix;

        private HintWithPrefix(String prefix_) {
            prefix = prefix_;
        }

        @Override
        protected boolean matchesSafely(Hint hint, Description mismatchDescription) {
            if (!hint.getInactivePrefix().equals(prefix)) {
                mismatchDescription.appendText("Prefix was <" + hint.getInactivePrefix() + ">");
                return false;
            } else {
                return true;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("A hint with prefix <" + prefix + ">");
        }
    }
}
