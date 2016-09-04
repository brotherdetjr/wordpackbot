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
            Session<V, T, S> session =
                    sessions.computeIfAbsent(event.getUserId(), userId -> initSession(userId, event.getChatId()));
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (session) {
                if (!session.isBusy()) {
                    session.setBusy(true);
                    callOnUpdate(event, session);
                } else {
                    send(NOT_SO_FAST_MESSAGE, event.getChatId());
                }
            }
        }));
    }

    private Session<V, T, S> initSession(long userId, long chatId) {
        Session<V, T, S> session = new Session<>(null, false);
        initialState(userId).whenComplete((result, ex) -> {
            if (ex == null) {
                session.setState(result);
            } else {
                sessions.remove(userId);
                send(getStackTraceAsString(ex), chatId);
            }
        });
        return session;
    }

    private void callOnUpdate(UpdateEvent event, Session<V, T, S> session) {
        S state = session.getState();
        onUpdate(event, state).whenComplete((transition, ex) -> {
            if (ex == null) {
                state.transit(transition).whenComplete(finishTransition(event, session));
            } else {
                send(getStackTraceAsString(ex), event.getChatId());
            }
        });
    }

    private BiConsumer<S, Throwable> finishTransition(UpdateEvent event, Session<V, T, S> session) {
        return (newState, ex) -> {
            if (ex == null) {
                session.setState(newState);
                session.setBusy(false);
            } else {
                send(getStackTraceAsString(ex), event.getChatId());
            }
        };
    }

    public void init() {
        init(directExecutor());
    }

    protected CompletableFuture<?> send(String text, Long chatId) {
        return bot.send(text, chatId);
    }

    protected abstract CompletableFuture<S> initialState(long userId);

    protected abstract CompletableFuture<T> onUpdate(UpdateEvent event, S state);

}
