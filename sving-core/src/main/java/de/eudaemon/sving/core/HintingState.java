package de.eudaemon.sving.core;

import java.util.*;
import java.util.stream.Collectors;

public class HintingState<CONTAINER, COMPONENT> {
    private final Hinter<CONTAINER, COMPONENT> hinter;
    private final List<Listener<COMPONENT>> listeners = new ArrayList<>();
    private final CONTAINER container;

    private boolean active = false;
    private Set<Hint<? extends COMPONENT>> hints;

    public HintingState(Hinter<CONTAINER, COMPONENT> hinter_, CONTAINER container_) {
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
            active = false;
            fireStop();
        } else if (hints.size() == 1) {
            hints.iterator().next().execute();
            fireStop();
        } else {
            fireHintsChangedEvent();
        }
    }

    public boolean isAwaitingInput() {
        return active;
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

        void showHints(Collection<Hint<? extends COMPONENT>> hints);

        void stopShowing();
    }
}
