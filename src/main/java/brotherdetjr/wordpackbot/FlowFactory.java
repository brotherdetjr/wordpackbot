package brotherdetjr.wordpackbot;

import brotherdetjr.pauline.core.Flow;
import brotherdetjr.pauline.telegram.TelegramRenderer;
import brotherdetjr.pauline.telegram.events.TelegramEvent;
import brotherdetjr.wordpackbot.states.Playback;
import brotherdetjr.wordpackbot.states.StateFactory;

import static brotherdetjr.pauline.telegram.TelegramFlowConfigurer.flow;

public class FlowFactory {

	private final StateFactory stateFactory;

	public FlowFactory(StateFactory stateFactory) {
		this.stateFactory = stateFactory;
	}

	public Flow.Builder<TelegramRenderer, TelegramEvent> create() {
		return flow()
			.initial(evt -> stateFactory.startPlayback(evt.getUserId(), "тест"))
			.handle().when(Playback.class).with((evt, playback) -> playback.next())
			.render(Playback.class).as(ctx -> ctx.getRenderer().send(ctx.getState().getValue()));
	}
}
