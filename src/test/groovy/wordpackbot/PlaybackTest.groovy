package wordpackbot

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.BlockingVariable
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.Playback
import wordpackbot.states.StateFactory

import java.util.concurrent.CompletableFuture

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
            CompletableFuture<Playback> playback

    @Unroll
    def 'next word is #expected'() {
        given:
        def result = new BlockingVariable<String>()
        playback = playback ? playback.get().transit('next') : stateFactory.startPlayback(188589442L, 'тест')
        expect:
        playback.whenComplete { res, ex -> result.set res.value }
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