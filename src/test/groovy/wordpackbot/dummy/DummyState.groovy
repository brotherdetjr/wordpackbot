package wordpackbot.dummy

import wordpackbot.states.State

import java.util.concurrent.CompletableFuture

import static java.util.concurrent.CompletableFuture.completedFuture

class DummyState implements State<Integer, Integer, DummyState> {
    private final int value

    DummyState(int value) { this.value = value }

    @Override
    Integer getValue() { value }

    static CompletableFuture<DummyState> futureDummyState(int value) {
        completedFuture new DummyState(value)
    }

    @Override
    CompletableFuture<DummyState> transit(Integer transition) {
        futureDummyState(value + transition)
    }

    @Override
    public String toString() { "DummyState{value=$value}" }
}
