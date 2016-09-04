package wordpackbot.dao

import java.util.concurrent.CompletableFuture

import static java.util.Collections.shuffle
import static java.util.concurrent.CompletableFuture.completedFuture

class StubPlaybackSourceDao implements PlaybackSourceDao {

    private final ConfigObject config
    private final Random random

    StubPlaybackSourceDao(ConfigObject config, Random random = new Random()) {
        this.config = config
        this.random = random
    }

    @Override
    CompletableFuture<Collection<Collection<String>>> shuffled(long userId, String wordPackName) {
        def source = [] + config.wordPacks[Long.toString(userId)][wordPackName].content
        shuffle source, random
        completedFuture source
    }
}
