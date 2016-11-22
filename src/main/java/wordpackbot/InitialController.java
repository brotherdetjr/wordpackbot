package wordpackbot;

import wordpackbot.bots.UpdateEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


public class InitialController<S> extends NamedViewsAware implements Controller<Void, S> {

	private final Function<UpdateEvent, CompletableFuture<ViewNameAndState<S>>> function;

	public InitialController(Function<UpdateEvent, CompletableFuture<ViewNameAndState<S>>> function,
							 Map<String, View<?>> views) {
		super(views);
		this.function = function;
	}

	@Override
	public CompletableFuture<ViewAndState<S>> transit(UpdateEvent event, Void ignore) {
		return function.apply(event).thenApply(nameAndState ->
			new ViewAndState<>(getViews().get(nameAndState.getName()), nameAndState.getState()));
	}
}
