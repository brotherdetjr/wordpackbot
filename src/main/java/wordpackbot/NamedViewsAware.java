package wordpackbot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor
public class NamedViewsAware {
	@Getter(PROTECTED)
	private final Map<String, View<?>> views;
}
