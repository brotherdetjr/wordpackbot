package wordpackbot.dao

import io.vertx.groovy.core.Future

interface PlaybackSourceDao {
    Future<Collection<Collection<String>>> shuffled(Long userId, String wordPackName)
}
