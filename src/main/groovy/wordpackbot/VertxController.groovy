package wordpackbot

import groovy.util.logging.Log4j2
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import wordpackbot.bots.Bot
import wordpackbot.bots.UpdateEvent
import wordpackbot.states.State

import static com.google.common.base.Throwables.getStackTraceAsString

@Log4j2
abstract class VertxController<T> {
    public static final String NOT_SO_FAST_MESSAGE = 'Wait, no so fast!'
    private final Bot bot
    // should be private
    protected final Map<Long, Session<T>> sessions = [:]

    VertxController(Vertx vertx, Bot bot) {
        this.bot = bot
        bot.onUpdate { UpdateEvent event ->
            vertx.getOrCreateContext().runOnContext {
                if (sessions[event.userId] == null || !sessions[event.userId].busy) {
                    if (sessions[event.userId] != null) {
                        sessions[event.userId].busy = true
                    }
                    doUpdate event
                } else {
                    bot.send NOT_SO_FAST_MESSAGE, event.chatId
                }
            }
        }
    }

    // should be private
    protected void doUpdate(UpdateEvent event) {
        if (!sessions.containsKey(event.userId)) {
            initialState(event.userId).setHandler {
                if (it.cause() == null) {
                    sessions[event.userId] = new Session(it.result())
                    transit event, it.result()
                } else {
                    bot.send getStackTraceAsString(it.cause()), event.chatId
                }
            }
        } else {
            transit event, sessions[event.userId].state
        }
    }

    // should be private
    protected void transit(UpdateEvent event, State<T> state) {
        onUpdate(event, state).setHandler {
            if (it.cause() == null) {
                sessions[event.userId].state.transit(it.result()).setHandler {
                    sessions[event.userId].state = it.result()
                    sessions[event.userId].busy = false
                }
            } else {
                bot.send getStackTraceAsString(it.cause()), event.chatId
            }
        }
    }

    protected Future<Object> send(String text, Long chatId) {
        bot.send text, chatId
    }

    protected abstract Future<? extends State<T>> initialState(Long userId)

    protected abstract Future<T> onUpdate(UpdateEvent event, State<T> state)

    static class Session<T> {
        public boolean busy
        public State<T> state

        Session(State<T> initialState) {
            this.state = initialState
        }
    }
}
