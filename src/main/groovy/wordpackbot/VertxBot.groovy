package wordpackbot
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import wordpackbot.states.State

abstract class VertxBot extends TelegramLongPollingBot {
    private final Vertx vertx
    protected final ConfigObject config
    // should be private, but does not work
    protected final Map<Long, State> sessions = [:]

    VertxBot(Vertx vertx, ConfigObject config) {
        this.vertx = vertx
        this.config = config
    }

    @Override
    String getBotToken() {
        config.bot.token
    }

    @Override
    void onUpdateReceived(Update update) {
        vertx.getOrCreateContext().runOnContext {
            def userId = extractUserId(update)
            if (!sessions.containsKey(userId)) {
                initialState(userId).setHandler {
                    sessions[userId] = it.result()
                    onUpdate update, new StateWrapper(userId)
                }
            } else {
                onUpdate update, new StateWrapper(userId)
            }
        }
    }

    protected abstract Future<State> initialState(Long userId)

    protected static Long extractUserId(Update update) {
        if (update.hasMessage()) {
            return update.message.from.id
        } else if (update.hasInlineQuery()) {
            return update.inlineQuery.from.id
        } else if (update.hasCallbackQuery()) {
            return update.callbackQuery.from.id
        } else if (update.hasChosenInlineQuery()) {
            return update.chosenInlineQuery.from.id
        } else if (update.hasEditedMessage()) {
            return update.editedMessage.from.id
        } else {
            throw new IllegalArgumentException()
        }
    }

    protected void send(SendMessage message, Handler<AsyncResult> callback) {
        vertx.executeBlocking({
            it.complete sendMessage(message)
        }, callback)
    }

    protected abstract void onUpdate(Update update, StateWrapper stateWrapper)

    @Override
    String getBotUsername() {
        config.bot.name
    }

    class StateWrapper {

        private final Long userId

        StateWrapper(Long userId) {
            this.userId = userId
        }

        void transit(Object transition) {
            // TODO block onUpdate handling until transition is complete.
            // Ideally enqueue all the calls and perform them just right after the transition has been completed.
            sessions[userId].transit(transition).setHandler {
                 sessions[userId] = it.result()
            }
        }

        def <T> T getValue() { sessions[userId].value }

        Long getUserId() { userId }
    }
}
