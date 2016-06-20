package wordpackbot.states
import io.vertx.groovy.core.Future
import wordpackbot.dao.PlaybackSourceDao

import static io.vertx.groovy.core.Future.future
import static wordpackbot.states.Playback.END_OF_ENTRY
import static groovy.lang.Closure.IDENTITY

class StateFactory {
    private final PlaybackSourceDao playbackSourceDao

    StateFactory(PlaybackSourceDao playbackSourceDao) {
        this.playbackSourceDao = playbackSourceDao
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    Future<State> startPlayback(Long userId, String wordPackName) {
        def result = future()
        Future<Collection<Collection<String>>> source = playbackSourceDao.shuffled userId, wordPackName
        source.setHandler {
            if (it.cause() == null) {
                def entryIterator = it.result().collect({ it + END_OF_ENTRY }).collectMany(IDENTITY).iterator()
                def value = entryIterator.next() as String
                result.complete new Playback(value, entryIterator, { startPlayback userId, wordPackName })
            } else {
                result.fail it.cause()
            }
        }
        result
    }
}
