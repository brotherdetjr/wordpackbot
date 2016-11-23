package wordpackbot;

import lombok.RequiredArgsConstructor;
import wordpackbot.bots.UpdateEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class ControllerImpl<O, N> implements Controller<O, N> {

	private final BiFunction<UpdateEvent, O, CompletableFuture<ViewNameAndState<N>>> function;
	private final Map<String, View<?>> views;

	@Override
	public CompletableFuture<ViewAndState<N>> transit(UpdateEvent event, O state) {
		return function.apply(event, state).thenApply(nameAndState ->
			new ViewAndState<>(views.get(nameAndState.getName()), nameAndState.getState()));
	}

}
