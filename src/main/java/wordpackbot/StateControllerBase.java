package wordpackbot;

import com.google.common.util.concurrent.Striped;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import wordpackbot.bots.ChatBot;
import wordpackbot.bots.UpdateEvent;
import wordpackbot.states.State;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;

import static com.google.common.base.Throwables.getStackTraceAsString;

@Log4j2
@RequiredArgsConstructor
public abstract class StateControllerBase<V, T, S extends State<V, T, S>> {
    private static final String NOT_SO_FAST_MESSAGE = "Wait, no so fast!";

    private final ChatBot bot;
    private final Map<Long, Session<V, T, S>> sessions;
    private final Executor executor;
    private final Striped<Lock> striped = Striped.lock(1000);

    public void init() {
        bot.onUpdate(event -> executor.execute(() -> {
            log.debug("Received event {}", event);
            Long userId = event.getUserId();
            Lock lock = striped.get(userId);
            try {
                lock.lock();
                Session<V, T, S> session = sessions.get(userId);
                if (session != null) {
                    log.debug("Current state for user {}: {}", userId, session.getState());
                    processIfNotBusy(event);
                } else {
                    log.debug("No session for user {} yet. Creating a new one.");
                    initSessionAndProcess(event);
                }
            } finally {
                lock.unlock();
            }
        }));
    }

    private void processIfNotBusy(UpdateEvent event) {
        Session<V, T, S> session = sessions.get(event.getUserId());
        if (!session.isBusy()) {
            session.setBusy(true);
            process(event, session);
        } else {
            send(NOT_SO_FAST_MESSAGE, event.getChatId());
        }
    }

    private Session<V, T, S> initSessionAndProcess(UpdateEvent event) {
        Session<V, T, S> session = new Session<>(null, false);
        long userId = event.getUserId();
        sessions.put(userId, session);
        initialState(userId).whenComplete((result, ex) -> executor.execute(() -> {
            if (ex == null) {
                session.setState(result);
                log.debug("Set initial state for user {}: {}", userId, result);
                process(event, session);
            } else {
                sessions.remove(userId);
                send(getStackTraceAsString(ex), event.getChatId());
            }
        }));
        return session;
    }

    private void process(UpdateEvent event, Session<V, T, S> session) {
        S state = session.getState();
        onUpdate(event, state).whenComplete((transition, ex) -> executor.execute(() -> {
            if (ex == null) {
                state.transit(transition).whenComplete(finishTransition(event, session));
            } else {
                send(getStackTraceAsString(ex), event.getChatId());
            }
        }));
    }

    private BiConsumer<S, Throwable> finishTransition(UpdateEvent event, Session<V, T, S> session) {
        return (newState, ex) -> {
            if (ex == null) {
                session.setState(newState);
                session.setBusy(false);
                log.debug("Set new state for user {}: {}. Releasing the session lock.", event.getUserId(), newState);
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
