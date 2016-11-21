package wordpackbot;

import com.google.common.util.concurrent.Striped;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import wordpackbot.bots.ChatBot;
import wordpackbot.bots.UpdateEvent;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

@Log4j2
@RequiredArgsConstructor
public abstract class Mvc {
	private final ChatBot bot;
	private final Dispatcher dispatcher;
	private final View<IllegalStateException> notSoFastView;
	private final View<Throwable> failView;
	private final ConcurrentMap<Long, Session> sessions;
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
		Session session = sessions.get(event.getUserId());
		if (!session.isBusy()) {
			session.setBusy(true);
			process(event, session);
		} else {
			IllegalStateException ex = new IllegalStateException("Wait, not so fast!");
			notSoFastView.render(new RenderContext<>(session.getState(), ex, event, textSender(event.getChatId())));
		}
	}

	private Session initSessionAndProcess(UpdateEvent event) {
		Session session = new Session(null, true);
		long userId = event.getUserId();
		sessions.put(userId, session);
		dispatcher.dispatch(event).transit(event).whenComplete((result, ex) -> executor.execute(() -> {
			if (ex == null) {
				session.setState(result);
				log.debug("Set initial state for user {}: {}", userId, result);
				process(event, session);
			} else {
				sessions.remove(userId);
				failView.render(new RenderContext<>(session.getState(), ex, event, textSender(event.getChatId())));
			}
		}));
		return session;
	}

	private void process(UpdateEvent event, Session session) {
		Object state = session.getState();
		dispatcher
			.dispatch(event, state)
			.transit(event, state)
			.whenComplete((viewAndState, ex) -> executor.execute(() -> {
				if (ex == null) {
					Long userId = event.getUserId();
					synched(userId, sameSession -> {
						session.setState(viewAndState.getState());
						session.setBusy(false);
						log.debug("Set new state for user {}: {}. View: {}",
							userId, viewAndState.getState(), viewAndState.getView());
						//noinspection unchecked
						viewAndState.getView().render(
							new RenderContext<>(state, viewAndState.getState(), event, textSender(event.getChatId()))
						);
					});
				} else {
					failView.render(new RenderContext<>(state, ex, event, textSender(event.getChatId())));
				}
			}));
	}

	private void synched(Long userId, Consumer<Session> consumer) {
		Lock lock = striped.get(userId);
		try {
			lock.lock();
			consumer.accept(sessions.get(userId));
		} finally {
			lock.unlock();
		}
	}

	private Consumer<String> textSender(Long chatId) {
		return text -> bot.send(text, chatId);
	}
}
