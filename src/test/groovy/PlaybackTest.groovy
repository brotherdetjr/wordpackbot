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
            stateFactory = new StateFactory(new StubPlaybackSourceDao(config, new Random(0)))

    @Shared
            Future<State> playback

    @Unroll
    def 'next word is #expected'() {
        given:
        def result = new BlockingVariable<String>()
        playback = playback ? playback.result().transit('next') : stateFactory.startPlayback(188589442L, 'тест')
        expect:
        playback.setHandler { result.set(it.result().value as String) }
        result.get() == expected
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