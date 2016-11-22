package wordpackbot;

import static com.google.common.base.Throwables.getStackTraceAsString;

public class FailView<E extends Throwable> implements View<E> {

	private final static FailView<?> instance = new FailView<>();

	@Override
	public <O> void render(RenderContext<O, E> context) {
		context.send(getStackTraceAsString(context.getNewState()));
	}

	@SuppressWarnings("unchecked")
	public static <T> View<T> getInstance() {
		return (View<T>) instance;
	}

}
