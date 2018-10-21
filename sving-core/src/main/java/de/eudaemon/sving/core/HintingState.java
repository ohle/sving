package de.eudaemon.sving.core;

import java.util.*;
import java.util.stream.Collectors;

public class HintingState<CONTAINER> {
    private final Hinter<CONTAINER, ?> hinter;
    private final List<Listener> listeners = new ArrayList<>();
    private final CONTAINER container;

    private boolean active = false;
    private Set<Hint> hints;

    public HintingState(Hinter<CONTAINER, ?> hinter_, CONTAINER container_) {
        hinter = hinter_;
        container = container_;
    }

    public void hotkeyPressed() {
        active = true;
        hints = hinter.findHints(container).collect(Collectors.toSet());
        fireHintsChangedEvent();
    }

    public void escapePressed() {
        active = false;
        hints = Collections.emptySet();
        fireStop();
    }

    public void keyPressed(char key) {
        if (!active) {
            return;
        }
        hints = hints.stream()
                .filter(h -> h.getActiveSuffix().startsWith(String.valueOf(key)))
                .map(Hint::afterNextKey)
                .collect(Collectors.toSet());
        if (hints.isEmpty()) {
            fireStop();
        } else {
            fireHintsChangedEvent();
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private void fireHintsChangedEvent() {
        listeners.forEach(l -> l.showHints(hints));
    }

    private void fireStop() {
        listeners.forEach(Listener::stopShowing);
    }

    public interface Listener<COMPONENT> {

        void showHints(Collection<Hint<COMPONENT>> hints);

        void stopShowing();
    }
}
