package wordpackbot

import groovy.util.logging.Log4j2
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import org.json.JSONObject
import org.telegram.telegrambots.api.methods.BotApiMethod
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.updateshandlers.SentCallback
import wordpackbot.states.State

import static com.google.common.base.Throwables.getStackTraceAsString
import static io.vertx.core.Future.failedFuture
import static io.vertx.groovy.core.Future.succeededFuture
import static wordpackbot.UpdateUtils.extractUserId

@Log4j2
abstract class VertxBot extends TelegramLongPollingBot {
    // should be private
    protected final Vertx vertx
    protected final ConfigObject config
    // should be private
    protected final Map<Long, State> sessions = [:]

    VertxBot(Vertx vertx, ConfigObject config) {
        this.vertx = vertx
        this.config = config
    }

    @Override
    String getBotToken() { config.bot.token }

    @Override
    void onUpdateReceived(Update update) {
        vertx.getOrCreateContext().runOnContext {
            def userId = extractUserId(update)
            if (!sessions.containsKey(userId)) {
                initialState(userId).setHandler {
                    if (it.cause() == null) {
                        sessions[userId] = it.result()
                        onUpdate update, new StateWrapper(userId)
                    } else {
                        reportError it.cause(), update.message.chatId
                    }
                }
            } else {
                onUpdate update, new StateWrapper(userId)
            }
        }
    }

    def reportError(Throwable throwable, Long chatId) {
        send new SendMessage(text: getStackTraceAsString(throwable), chatId: Long.toString(chatId))
    }

    protected abstract Future<State> initialState(Long userId)

    protected void send(SendMessage message, Handler<AsyncResult> callback =
            { log.info "Message ${message.text} sent to chat ${message.chatId}" }) {
        sendMessageAsync message, new SentCallback<Message>() {
            @Override
            void onResult(BotApiMethod<Message> method, JSONObject jsonObject) {
                vertx.runOnContext {
                    callback.handle succeededFuture(method: method, jsonObject: jsonObject).delegate as AsyncResult
                }
            }

            @Override
            void onError(BotApiMethod<Message> method, JSONObject jsonObject) {
                vertx.runOnContext {
                    callback.handle failedFuture(new PayloadException(method: method, jsonObject: jsonObject))
                }
            }

            @Override
            void onException(BotApiMethod<Message> method, Exception exception) {
                vertx.runOnContext {
                    callback.handle failedFuture(exception)
                }
            }
        }
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
            sessions[userId].transit(transition).setHandler {
                sessions[userId] = it.result()
            }
        }

        def <T> T getValue() { sessions[userId].value }

        Long getUserId() { userId }
    }
}
