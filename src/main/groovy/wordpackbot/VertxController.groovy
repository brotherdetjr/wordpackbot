package wordpackbot

import groovy.util.logging.Log4j2
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import wordpackbot.bots.Bot
import wordpackbot.bots.UpdateEvent
import wordpackbot.states.State

import static com.google.common.base.Throwables.getStackTraceAsString

@Log4j2
abstract class VertxController {
    public static final String NOT_SO_FAST_MESSAGE = 'Wait, no so fast!'
    protected final Bot bot
    // should be private
    protected final Map<Long, Session> sessions = [:]

    VertxController(Vertx vertx, Bot bot) {
        this.bot = bot
        bot.onUpdate({ UpdateEvent event ->
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
        }).onError { Throwable t ->
            vertx.getOrCreateContext().runOnContext {
                log.error 'Bot issued an error', t
            }
        }
    }

    private void doUpdate(UpdateEvent event) {
        def transitTo = {
            sessions[event.userId].state.transit(it).setHandler {
                sessions[event.userId].state = it.result()
                sessions[event.userId].busy = false
            }
        }
        if (!sessions.containsKey(event.userId)) {
            initialState(event.userId).setHandler {
                if (it.cause() == null) {
                    sessions[event.userId] = new Session(it.result())
                    onUpdate event, it.result(), transitTo
                } else {
                    bot.send getStackTraceAsString(it.cause()), event.chatId
                }
            }
        } else {
            onUpdate event, sessions[event.userId].state, transitTo
        }
    }

    protected abstract Future<State> initialState(Long userId)

    protected abstract void onUpdate(UpdateEvent event, State state, Closure transitTo)

    class Session {
        public boolean busy
        public State state

        Session(State initialState) {
            this.state = initialState
        }
    }
}
