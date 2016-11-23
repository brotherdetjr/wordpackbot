package wordpackbot;

import com.google.common.util.concurrent.Striped;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import wordpackbot.bots.ChatBot;
import wordpackbot.bots.UpdateEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

@Log4j2
@RequiredArgsConstructor
public class Mvc {
	private final ChatBot bot;
	private final Dispatcher dispatcher;
	private final View<IllegalStateException> notSoFastView;
	private final View<Throwable> failView;
	private final Executor executor;
	private final ConcurrentMap<Long, Session> sessions;
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
		try {
			processUnsafe(event, session, state);
		} catch (Exception ex) {
			failView.render(new RenderContext<>(state, ex, event, textSender(event.getChatId())));
		}
	}

	private void processUnsafe(UpdateEvent event, Session session, Object state) {
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

	@RequiredArgsConstructor
	public static class Builder {
		private final ChatBot bot;
		private Map<String, View<?>> views = newHashMap();
		private Map<Class<?>, Controller<?, ?>> controllers = newHashMap();
		private View<IllegalStateException> notSoFastView = FailView.getInstance();
		private View<Throwable> failView = FailView.getInstance();
		private Executor executor = directExecutor();
		private ConcurrentMap<Long, Session> sessions = newConcurrentMap();
		private Controller<?, ?> initial;

		public <O, N> Builder rawController(Class<O> probe, Controller<O, N> controller) {
			controllers.put(probe, controller);
			return this;
		}

		public <O, N> Builder funcController(
			Class<O> probe,
			BiFunction<UpdateEvent, O, CompletableFuture<ViewNameAndState<N>>> function) {
			return rawController(probe, new ControllerImpl<>(function, views));
		}

		public <O, N> Builder controller(
			Class<O> probe,
			BiFunction<UpdateEvent, O, CompletableFuture<N>> function) {
			return funcController(
				probe,
				(e, s) -> function.apply(e, s).thenApply(n -> ViewNameAndState.of(e.getClass(), n))
			);
		}

		public <O, N> Builder initialController(Controller<O, N> initial) {
			this.initial = initial;
			return this;
		}

		public <N> Builder initialRaw(Function<UpdateEvent, CompletableFuture<ViewNameAndState<N>>> function) {
			return initialController(new ControllerImpl<Void, N>((e, ignore) -> function.apply(e), views));
		}

		public <N> Builder initial(Function<UpdateEvent, CompletableFuture<N>> function) {
			return initialRaw(e -> function.apply(e).thenApply(s -> new ViewNameAndState<>(s.getClass().getName(), s)));
		}

		public <N> Builder view(String name, View<N> view) {
			views.put(name, view);
			return this;
		}

		public <N> Builder view(Class<N> probe, View<N> view) {
			return view(probe.getName(), view);
		}

		public Builder notSoFastView(View<IllegalStateException> notSoFastView) {
			this.notSoFastView = notSoFastView;
			return this;
		}

		public Builder failView(View<Throwable> failView) {
			this.failView = failView;
			return this;
		}

		public Builder executor(Executor executor) {
			this.executor = executor;
			return this;
		}

		public Builder sessions(ConcurrentMap<Long, Session> sessions) {
			this.sessions = sessions;
			return null;
		}

		public Mvc build(boolean initialized) {
			Mvc mvc = new Mvc(
				bot,
				new DispatcherImpl(controllers, initial),
				notSoFastView,
				failView,
				executor,
				sessions
			);
			if (initialized) {
				mvc.init();
			}
			return mvc;
		}

		public Mvc build() {
			return build(true);
		}
	}
}
