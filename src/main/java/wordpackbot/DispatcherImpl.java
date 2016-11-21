package wordpackbot;

import lombok.RequiredArgsConstructor;
import wordpackbot.bots.UpdateEvent;

import java.util.Map;

@RequiredArgsConstructor
public class DispatcherImpl implements Dispatcher {

	private final Map<Class<?>, Controller<?, ?>> controllers;
	private final Controller<?, ?> initial;

	@Override
	@SuppressWarnings("unchecked")
	public <O> Controller<O, ?> dispatch(UpdateEvent ignore, O state) {
		return state != null ? (Controller<O, ?>) controllers.get(state.getClass()) : (Controller<O, ?>) initial;
	}
}
