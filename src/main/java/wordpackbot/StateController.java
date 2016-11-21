package wordpackbot;

import com.google.common.util.concurrent.Striped;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import wordpackbot.bots.ChatBot;
import wordpackbot.bots.UpdateEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import static com.google.common.base.Throwables.getStackTraceAsString;

@Log4j2
@RequiredArgsConstructor
public abstract class StateController<S> {
    private static final String NOT_SO_FAST_MESSAGE = "Wait, no so fast!";

    private final ChatBot bot;
    private final ConcurrentMap<Long, Session<S>> sessions;
    private final Executor executor;
    private final Striped<Lock> striped = Striped.lock(1000);

    public void init() {
        bot.onUpdate(event -> executor.execute(() -> {
            log.debug("Received event {}", event);
            Long userId = event.getUserId();
            synched(userId, session -> {
                if (session != null) {
                    log.debug("Current session for user {}: {}", userId, session);
                    processIfNotBusy(event);
                } else {
                    log.debug("No session for user {} yet. Creating a new one.");
                    initSessionAndProcess(event);
                }
            });
        }));
    }

    private void processIfNotBusy(UpdateEvent event) {
        Session<S> session = sessions.get(event.getUserId());
        if (!session.isBusy()) {
            session.setBusy(true);
            process(event, session);
        } else {
            bot.send(NOT_SO_FAST_MESSAGE, event.getChatId());
        }
    }

    private Session<S> initSessionAndProcess(UpdateEvent event) {
        Session<S> session = new Session<>(null, true);
        long userId = event.getUserId();
        sessions.put(userId, session);
        initialState(userId).whenComplete((result, ex) -> executor.execute(() -> {
            if (ex == null) {
                session.setState(result);
                log.debug("Set initial state for user {}: {}", userId, result);
                process(event, session);
            } else {
                sessions.remove(userId);
                bot.send(getStackTraceAsString(ex), event.getChatId());
            }
        }));
        return session;
    }

    private void process(UpdateEvent event, Session<S> session) {
        S state = session.getState();
        onUpdate(event, state).whenComplete((newState, ex) -> executor.execute(() -> {
            if (ex == null) {
                Long userId = event.getUserId();
                synched(userId, sameSession -> {
                    session.setState(newState);
                    session.setBusy(false);
                    log.debug("Set new state for user {}: {}", userId, newState);
                    afterTransition(new RenderContext<>(state, newState, event, bot::send));
                });
            } else {
                bot.send(getStackTraceAsString(ex), event.getChatId());
            }
        }));
    }

    private void synched(Long userId, Consumer<Session<S>> consumer) {
        Lock lock = striped.get(userId);
        try {
            lock.lock();
            consumer.accept(sessions.get(userId));
        } finally {
            lock.unlock();
        }
    }

    protected abstract CompletableFuture<S> initialState(long userId);

    protected abstract CompletableFuture<S> onUpdate(UpdateEvent event, S state);

    protected abstract void afterTransition(RenderContext<S> context);

}
