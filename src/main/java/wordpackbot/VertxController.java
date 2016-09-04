package wordpackbot;

import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import wordpackbot.bots.ChatBot;
import wordpackbot.bots.UpdateEvent;
import wordpackbot.states.State;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.google.common.base.Throwables.getStackTraceAsString;

@Log4j2
@RequiredArgsConstructor
public abstract class VertxController<V1, T1, S1 extends State<V1, T1, S1>> {
    private static final String NOT_SO_FAST_MESSAGE = "Wait, no so fast!";

    private final ChatBot bot;
    private final Map<Long, Session<V1, T1, S1>> sessions;

    public void init(Vertx vertx) {
        bot.onUpdate(event -> vertx.getOrCreateContext().runOnContext(ignore -> {
            log.debug("Processing event {}", event);
            Session session = sessions.get(event.getUserId());
            if (session == null || !session.isBusy()) {
                if (session != null) {
                    session.setBusy(true);
                }
                doUpdate(event);
            } else {
                send(NOT_SO_FAST_MESSAGE, event.getChatId());
            }
        }));
    }

    private void doUpdate(UpdateEvent event) {
        Session<V1, T1, S1> session = sessions.get(event.getUserId());
        if (session == null) {
            initialState(event.getUserId()).whenComplete((result, ex) -> {
                if (ex == null) {
                    sessions.put(event.getUserId(), new Session<>(result));
                    transit(event, result);
                } else {
                    send(getStackTraceAsString(ex), event.getChatId());
                }
            });
        } else {
            transit(event, session.getState());
        }
    }

    private void transit(UpdateEvent event, S1 state) {
        onUpdate(event, state).whenComplete((result, ex) -> {
            if (ex == null) {
                state.transit(result).whenComplete(finishTransition(event));
            } else {
                send(getStackTraceAsString(ex), event.getChatId());
            }
        });
    }

    private BiConsumer<S1, Throwable> finishTransition(UpdateEvent event) {
        return (result, ex) -> {
            if (ex == null) {
                Session<V1, T1, S1> session = sessions.get(event.getUserId());
                session.setState(result);
                session.setBusy(false);
            } else {
                send(getStackTraceAsString(ex), event.getChatId());
            }
        };
    }

    protected CompletableFuture<?> send(String text, Long chatId) {
        return bot.send(text, chatId);
    }

    protected abstract CompletableFuture<S1> initialState(long userId);

    protected abstract CompletableFuture<T1> onUpdate(UpdateEvent event, S1 state);

}
