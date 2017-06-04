package wordpackbot.dao

import java.util.concurrent.CompletableFuture

import static java.util.concurrent.CompletableFuture.completedFuture

class StubPlaybackSourceDao implements PlaybackSourceDao {

    private final ConfigObject config

    StubPlaybackSourceDao(ConfigObject config) {
        this.config = config
    }

    @Override
    CompletableFuture<Collection<Collection<String>>> getWordPack(long userId, String wordPackName) {
        completedFuture([] + config.wordPacks[Long.toString(userId)][wordPackName].content)
    }
}
