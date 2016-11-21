package wordpackbot;

import static com.google.common.base.Throwables.getStackTraceAsString;

public class FailView<E extends Throwable> implements View<E> {

	@Override
	public <O> void render(RenderContext<O, E> context) {
		context.send(getStackTraceAsString(context.getNewState()));
	}

}
