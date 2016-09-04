package wordpackbot.states;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;

@RequiredArgsConstructor
public class Playback implements State<String, String, Playback> {

    @SuppressWarnings("RedundantStringConstructorCall")
    public final static String END_OF_ENTRY = new String();

    @Getter private final String value;
    private final Iterator<String> entryIterator;
    private final Supplier<CompletableFuture<Playback>> onFinish;

    @Override
    public CompletableFuture<Playback> transit(String transition) {
        String value = entryIterator.next();
        if (END_OF_ENTRY.equals(value)) {
            value = entryIterator.hasNext() ? entryIterator.next() : null;
        }
        return value != null ? completedFuture(new Playback(value, entryIterator, onFinish)) : onFinish.get();
    }
}
