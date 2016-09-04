package wordpackbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import wordpackbot.bots.ChatBot;
import wordpackbot.bots.UpdateEvent;
import wordpackbot.states.State;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

@Log4j2
@RequiredArgsConstructor
public abstract class StateControllerBase<V, T, S extends State<V, T, S>> {
    private static final String NOT_SO_FAST_MESSAGE = "Wait, no so fast!";

    private final ChatBot bot;
    private final Map<Long, Session<V, T, S>> sessions;

    public void init(Executor executor) {
        bot.onUpdate(event -> executor.execute(() -> {
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

    public void init() {
        init(directExecutor());
    }

    private void doUpdate(UpdateEvent event) {
        Session<V, T, S> session = sessions.get(event.getUserId());
        if (session == null) {
            initialState(event.getUserId()).whenComplete((result, ex) -> {
                if (ex == null) {
                    sessions.put(event.getUserId(), new Session<>(result, true));
                    transit(event, result);
                } else {
                    send(getStackTraceAsString(ex), event.getChatId());
                }
            });
        } else {
            transit(event, session.getState());
        }
    }

    private void transit(UpdateEvent event, S state) {
        onUpdate(event, state).whenComplete((result, ex) -> {
            if (ex == null) {
                state.transit(result).whenComplete(finishTransition(event));
            } else {
                send(getStackTraceAsString(ex), event.getChatId());
            }
        });
    }

    private BiConsumer<S, Throwable> finishTransition(UpdateEvent event) {
        return (result, ex) -> {
            if (ex == null) {
                Session<V, T, S> session = sessions.get(event.getUserId());
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

    protected abstract CompletableFuture<S> initialState(long userId);

    protected abstract CompletableFuture<T> onUpdate(UpdateEvent event, S state);

}
