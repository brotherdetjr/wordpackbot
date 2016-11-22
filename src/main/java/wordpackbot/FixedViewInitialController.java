package wordpackbot;

import lombok.RequiredArgsConstructor;
import wordpackbot.bots.UpdateEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@RequiredArgsConstructor
public class FixedViewInitialController<S> implements Controller<Void, S> {

	private final Function<UpdateEvent, CompletableFuture<S>> function;
	private final View<S> view;

	@Override
	public CompletableFuture<ViewAndState<S>> transit(UpdateEvent event, Void ignore) {
		return function.apply(event).thenApply(s -> new ViewAndState<>(view, s));
	}

}
