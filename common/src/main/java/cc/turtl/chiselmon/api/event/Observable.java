package cc.turtl.chiselmon.api.event;

import cc.turtl.chiselmon.api.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Observable<T> {
    private final List<Handler<T>> handlers = new ArrayList<>();

    public void subscribe(Consumer<T> action) {
        subscribe(Priority.NORMAL, action);
    }

    public void subscribe(Priority priority, Consumer<T> action) {
        handlers.add(new Handler<>(priority, action));
        handlers.sort((a, b) -> a.priority.compareTo(b.priority));
    }

    public void emit(T value) {
        for (Handler<T> handler : handlers) {
            handler.action.accept(value);
        }
    }

    private record Handler<T>(Priority priority, Consumer<T> action) {
    }
}