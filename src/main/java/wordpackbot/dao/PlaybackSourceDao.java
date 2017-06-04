package wordpackbot.dao;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface PlaybackSourceDao {
    CompletableFuture<Collection<Collection<String>>> getWordPack(long userId, String wordPackName);
}
