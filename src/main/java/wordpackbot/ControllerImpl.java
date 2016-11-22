package wordpackbot;

import wordpackbot.bots.UpdateEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class ControllerImpl<O, N> extends NamedViewsAware implements Controller<O, N> {

	private final BiFunction<UpdateEvent, O, CompletableFuture<ViewNameAndState<N>>> function;

	public ControllerImpl(BiFunction<UpdateEvent, O, CompletableFuture<ViewNameAndState<N>>> function,
						  Map<String, View<?>> views) {
		super(views);
		this.function = function;
	}

	@Override
	public CompletableFuture<ViewAndState<N>> transit(UpdateEvent event, O state) {
		return function.apply(event, state).thenApply(nameAndState ->
			new ViewAndState<>(getViews().get(nameAndState.getName()), nameAndState.getState()));
	}

}
