package wordpackbot.states;

import wordpackbot.Mvc;
import wordpackbot.StateController;

public class DispatcherImpl implements Mvc {
	@Override
	public StateController<?> init() {
		return null;
	}

	@Override
	public <S> StateController<S> dispatch(S state) {
		return null;
	}
}
