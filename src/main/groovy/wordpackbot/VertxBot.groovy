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
import static wordpackbot.UpdateUtils.extractChatId
import static wordpackbot.UpdateUtils.extractUserId

@Log4j2
abstract class VertxBot extends TelegramLongPollingBot {
    public static final String NOT_SO_FAST_MESSAGE = 'Wait, no so fast!'
    // should be private
    protected final Vertx vertx
    protected final ConfigObject config
    // should be private
    protected final Map<Long, Session> sessions = [:]

    VertxBot(Vertx vertx, ConfigObject config) {
        this.vertx = vertx
        this.config = config
    }

    @Override
    String getBotToken() { config.bot.token }

    @Override
    void onUpdateReceived(Update update) {
        vertx.getOrCreateContext().runOnContext {
            Long userId = extractUserId(update)
            if (sessions[userId] == null || !sessions[userId].busy) {
                if (sessions[userId] != null) {
                    sessions[userId].busy = true
                }
                doUpdate userId, update
            } else {
                send new SendMessage(text: NOT_SO_FAST_MESSAGE, chatId: extractChatId(update))
            }
        }
    }

    def void doUpdate(long userId, Update update) {
        def transitTo = {
            sessions[userId].state.transit(it).setHandler {
                sessions[userId].state = it.result()
                sessions[userId].busy = false
            }
        }
        if (!sessions.containsKey(userId)) {
            initialState(userId).setHandler {
                if (it.cause() == null) {
                    sessions[userId] = new Session(it.result())
                    onUpdate update, userId, it.result(), transitTo
                } else {
                    reportError it.cause(), extractChatId(update)
                }
            }
        } else {
            onUpdate update, userId, sessions[userId].state, transitTo
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

    protected abstract void onUpdate(Update update, Long userId, State state, Closure transitTo)

    @Override
    String getBotUsername() { config.bot.name }

    class Session {
        public boolean busy
        public State state

        Session(State initialState) {
            this.state = initialState
        }
    }
}
