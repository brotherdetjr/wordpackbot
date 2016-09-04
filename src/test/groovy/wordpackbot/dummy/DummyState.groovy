package wordpackbot.dummy

import sun.reflect.generics.scope.DummyScope
import wordpackbot.states.State

import java.util.concurrent.CompletableFuture

import static java.util.concurrent.CompletableFuture.completedFuture

class DummyState implements State<Integer, String, DummyState> {
    private final int value

    DummyState(int value) { this.value = value }

    @Override
    Integer getValue() { value }

    static CompletableFuture<DummyState> futureDummyState(int value) {
        completedFuture new DummyState(value)
    }

    @Override
    CompletableFuture<DummyState> transit(String transition) {
        futureDummyState(value + transition as int)
    }
}
