package brotherdetjr.wordpackbot.states;

import lombok.RequiredArgsConstructor;
import brotherdetjr.wordpackbot.dao.PlaybackSourceDao;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.Collections.singleton;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static brotherdetjr.wordpackbot.states.Playback.END_OF_ENTRY;

@RequiredArgsConstructor
public class StateFactory {

	private final PlaybackSourceDao playbackSourceDao;
	private final Random random;

	public StateFactory(PlaybackSourceDao playbackSourceDao) {
		this(playbackSourceDao, new Random());
	}

	public CompletableFuture<Playback> startPlayback(long userId, String wordPackName) {
		CompletableFuture<Playback> future = new CompletableFuture<>();
		playbackSourceDao.getWordPack(userId, wordPackName).whenComplete((result, ex) -> {
			if (ex == null) {
				List<Collection<String>> shuffled = newArrayList(result);
				shuffle(shuffled, random);
				Iterator<String> entryIterator = shuffled.stream()
					.map(this::appendEndOfEntry).flatMap(identity()).collect(toList()).iterator();
				String value = entryIterator.next();
				future.complete(new Playback(value, entryIterator, () -> startPlayback(userId, wordPackName)));
			} else {
				future.completeExceptionally(ex);
			}
		});
		return future;
	}

	private Stream<String> appendEndOfEntry(Collection<String> it) {
		return stream(concat(it, singleton(END_OF_ENTRY)).spliterator(), false);
	}
}
