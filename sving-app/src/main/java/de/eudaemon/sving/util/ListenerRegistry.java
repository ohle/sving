package de.eudaemon.sving.util;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ListenerRegistry<L extends EventListener> {
    private List<L> listeners = new CopyOnWriteArrayList<>();

    public void add(L listener) {
        listeners.add(listener);
    }

    public void remove(L listener) {
        listeners.remove(listener);
    }

    public void invoke(Consumer<L> callback) {
        listeners.forEach(callback);
    }
}
