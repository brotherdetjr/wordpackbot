package wordpackbot

import groovy.util.logging.Log4j2
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import wordpackbot.bots.Bot
import wordpackbot.bots.UpdateEvent
import wordpackbot.states.Playback
import wordpackbot.states.State
import wordpackbot.states.StateFactory

import static io.vertx.groovy.core.Future.succeededFuture

@Log4j2
class WordPackController extends VertxController<String> {

    private final StateFactory stateFactory

    WordPackController(Vertx vertx, Bot bot, StateFactory stateFactory) {
        super(vertx, bot)
        this.stateFactory = stateFactory
    }

    @Override
    Future<String> onUpdate(UpdateEvent update, State<String> state) {
        log.info "Sending message: $state.value"
        send state.value, update.chatId
        succeededFuture 'next'
    }

    @Override
    protected Future<Playback> initialState(Long userId) {
        stateFactory.startPlayback userId, 'тест'
    }
}
