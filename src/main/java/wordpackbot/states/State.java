package wordpackbot.states;

import java.util.concurrent.CompletableFuture;

public interface State<V, T, S extends State<V, T, S>> {
    V getValue();
    CompletableFuture<S> transit(T transition);
}
