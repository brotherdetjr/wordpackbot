package wordpackbot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wordpackbot.bots.UpdateEvent;

import java.util.concurrent.CompletableFuture;

public interface Controller<O, N> {

	CompletableFuture<ViewAndState<N>> transit(UpdateEvent event, O state);

	default CompletableFuture<ViewAndState<N>> transit(UpdateEvent event) {
		return transit(event, null);
	}

	@RequiredArgsConstructor
	@Getter
	class ViewAndState<S> {
		private final View view;
		private final S state;
	}
}
