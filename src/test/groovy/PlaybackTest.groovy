import io.vertx.groovy.core.Future
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.BlockingVariable
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.State
import wordpackbot.states.StateFactory


class PlaybackTest extends Specification {

    @Shared
            random = new Random(0)

    @Shared
            config = new ConfigSlurper().parse('''
wordPacks {
    '188589442' {
        'тест' {
            content = [
                    ['птичка', 'birdy'],
                    ['киска', 'pussy', "'паси"],
                    ['собачка', 'doggy', "'доги"]
            ]
        }
    }
}''')

    @Shared
            stateFactory = new StateFactory(new StubPlaybackSourceDao(config, random))

    @Shared
            Future<State> playback

    @Unroll
    def 'next word is #expected'() {
        given:
        def result = new BlockingVariable<Boolean>()
        playback = playback == null ? stateFactory.startPlayback(188589442L, 'тест') : playback.result().transit('next')
        expect:
        playback.setHandler { result.set(it.result().value == expected) }
        result.get()
        where:
        expected << ['собачка', 'doggy', "'доги",
                     'киска', 'pussy', "'паси",
                     'птичка', 'birdy',
                     'птичка', 'birdy',
                     'собачка', 'doggy', "'доги",
                     'киска', 'pussy', "'паси",
                     'киска', 'pussy', "'паси",
                     'птичка', 'birdy',
                     'собачка', 'doggy', "'доги"]
    }

}