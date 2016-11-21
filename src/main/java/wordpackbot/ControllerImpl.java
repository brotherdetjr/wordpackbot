package wordpackbot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wordpackbot.bots.UpdateEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class ControllerImpl<O, N> implements Controller<O, N> {

	private final Map<String, View<?>> views;
	private final BiFunction<UpdateEvent, O, CompletableFuture<ViewNameAndState<N>>> function;

	@Override
	public CompletableFuture<ViewAndState<N>> transit(UpdateEvent event, O state) {
		return function.apply(event, state).thenApply(nameAndState ->
			new ViewAndState<>(views.get(nameAndState.getName()), nameAndState.getState()));
	}

	@RequiredArgsConstructor
	@Getter
	public static class ViewNameAndState<S> {
		private final String name;
		private final S state;
	}
}
