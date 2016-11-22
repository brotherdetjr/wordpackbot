package wordpackbot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ViewNameAndState<S> {
	private final String name;
	private final S state;
}
