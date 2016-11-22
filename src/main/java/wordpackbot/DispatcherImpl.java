package wordpackbot;

import wordpackbot.bots.UpdateEvent;

import java.util.Map;

public class DispatcherImpl implements Dispatcher {

	private final Map<Class<?>, Controller<?, ?>> controllers;
	private final Controller<?, ?> initial;

	@java.beans.ConstructorProperties({"controllers", "initial"})
	public DispatcherImpl(Map<Class<?>, Controller<?, ?>> controllers, Controller<?, ?> initial) {
		if (controllers == null) {
			throw new IllegalArgumentException("controllers cannot be null");
		}
		if (initial == null) {
			throw new IllegalArgumentException("initial cannot be null");
		}
		this.controllers = controllers;
		this.initial = initial;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <O> Controller<O, ?> dispatch(UpdateEvent ignore, O state) {
		if (state != null) {
			Controller<?, ?> controller = controllers.get(state.getClass());
			if (controller == null) {
				throw new IllegalArgumentException("No controller registered for state type " + state.getClass());
			}
			return (Controller<O, ?>) controller;
		}
		else {
			return (Controller<O, ?>) initial;
		}
	}
}
