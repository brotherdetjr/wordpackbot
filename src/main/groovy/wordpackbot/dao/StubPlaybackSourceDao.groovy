package wordpackbot.dao
import io.vertx.groovy.core.Future

import static io.vertx.groovy.core.Future.succeededFuture
import static java.util.Collections.shuffle

class StubPlaybackSourceDao implements PlaybackSourceDao {

    private final ConfigObject config
    private final Random random

    StubPlaybackSourceDao(ConfigObject config, Random random = new Random()) {
        this.config = config
        this.random = random
    }

    @SuppressWarnings(["GrUnresolvedAccess", "GroovyAssignabilityCheck"])
    @Override
    Future<Collection<Collection<String>>> shuffled(Long userId, String wordPackName) {
        def source = [] + config.wordPacks[Long.toString(userId)][wordPackName].content
        shuffle source, random
        succeededFuture(source)
    }
}
