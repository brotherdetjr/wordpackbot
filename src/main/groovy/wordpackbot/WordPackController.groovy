package wordpackbot

import groovy.util.logging.Log4j2
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import wordpackbot.bots.Bot
import wordpackbot.bots.UpdateEvent
import wordpackbot.states.State
import wordpackbot.states.StateFactory

@Log4j2
class WordPackController extends VertxController {

    private final StateFactory stateFactory

    WordPackController(Vertx vertx, Bot bot, StateFactory stateFactory) {
        super(vertx, bot)
        this.stateFactory = stateFactory
    }

    @Override
    void onUpdate(UpdateEvent update, State state, Closure transitTo) {
        log.info "Sending message: $state.value"
        bot.send state.value as String, update.chatId
        transitTo 'next'
    }

    @Override
    protected Future<State> initialState(Long userId) {
        stateFactory.startPlayback userId, 'тест'
    }
}
