package dummy

import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import wordpackbot.VertxController
import wordpackbot.bots.Bot
import wordpackbot.bots.UpdateEvent
import wordpackbot.states.State

import static dummy.DummyState.futureDummyState

class DummyController extends VertxController<Integer> {

    DummyController(Vertx vertx, Bot bot) {
        super(vertx, bot)
    }

    @Override
    protected Future<DummyState> initialState(Long userId) { futureDummyState 33 }

    @Override
    protected Future<Integer> onUpdate(UpdateEvent event, State<Integer> state) {
        futureDummyState state.value + Integer.parseInt(event.text)
    }
}
