package wordpackbot;

public interface View<S> {
	<O> void render(RenderContext<O, S> context);
}
