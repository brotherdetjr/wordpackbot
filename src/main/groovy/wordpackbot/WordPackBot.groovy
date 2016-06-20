package wordpackbot
import groovy.util.logging.Log4j2
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import wordpackbot.states.State
import wordpackbot.states.StateFactory

@Log4j2
class WordPackBot extends VertxBot {

    private final StateFactory stateFactory

    WordPackBot(Vertx vertx, ConfigObject config, StateFactory stateFactory) {
        super(vertx, config)
        this.stateFactory = stateFactory
    }

    @Override
    void onUpdate(Update update, StateWrapper stateWrapper) {
        if (update.hasMessage()) {
            def message = update.message
            log.info "Update with message: $update"

            def toSend = new SendMessage(
                    text: stateWrapper.value,
                    chatId: Long.toString(message.chatId))
            log.info "Sending message: $toSend"
            send toSend

            stateWrapper.transit 'next'
        }
    }

    @Override
    protected Future<State> initialState(Long userId) {
        stateFactory.startPlayback userId, 'тест'
    }
}
