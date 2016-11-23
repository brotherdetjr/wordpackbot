package wordpackbot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ViewNameAndState<S> {
	private final String name;
	private final S state;

	public static <S> ViewNameAndState<S> of(String name, S state) {
		return new ViewNameAndState<>(name, state);
	}

	public static <S> ViewNameAndState<S> of(Class<? extends S> probe, S state) {
		return of(probe.getName(), state);
	}

}
