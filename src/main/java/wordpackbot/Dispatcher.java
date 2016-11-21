package wordpackbot;

import wordpackbot.bots.UpdateEvent;

public interface Dispatcher {
	<O> Controller<O, ?> dispatch(UpdateEvent event, O state);

	default <O> Controller<O, ?> dispatch(UpdateEvent event) {
		return dispatch(event, null);
	}
}
