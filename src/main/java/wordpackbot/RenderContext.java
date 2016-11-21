package wordpackbot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wordpackbot.bots.UpdateEvent;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class RenderContext<O, N> {
	@Getter
	private final O oldState;
	@Getter
	private final N newState;
	@Getter
	private final UpdateEvent event;
	private final Consumer<String> sender;

	public void send(String text) {
		sender.accept(text);
	}
}
