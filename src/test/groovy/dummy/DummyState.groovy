package dummy

import io.vertx.groovy.core.Future
import wordpackbot.states.State

import static io.vertx.core.Future.succeededFuture

class DummyState implements State<Integer> {
    private final int value

    DummyState(int value) { this.value = value }

    @Override
    Integer getValue() { value }

    static Future<State> futureDummyState(int value) {
        succeededFuture(new DummyState(value)) as Future<State>
    }

    @Override
    Future<DummyState> transit(Object transition) {
        futureDummyState(value + (transition as Integer)) as Future<DummyState>
    }
}
