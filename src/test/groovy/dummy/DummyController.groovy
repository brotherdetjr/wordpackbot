package dummy

import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import wordpackbot.VertxController
import wordpackbot.bots.Bot
import wordpackbot.bots.UpdateEvent
import wordpackbot.states.State

import static dummy.DummyState.futureDummyState
import static io.vertx.groovy.core.Future.succeededFuture

class DummyController extends VertxController<Integer> {

    private final initialValue

    DummyController(Vertx vertx, Bot bot, int initialValue) {
        super(vertx, bot)
        this.initialValue = initialValue
    }

    @Override
    protected Future<DummyState> initialState(Long userId) { futureDummyState initialValue }

    @Override
    protected Future<Integer> onUpdate(UpdateEvent event, State<Integer> state) {
        def increment = Integer.parseInt(event.text)
        send Integer.toString(state.value + increment), event.chatId
        succeededFuture increment
    }
}
