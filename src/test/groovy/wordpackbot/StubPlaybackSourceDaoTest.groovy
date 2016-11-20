package wordpackbot

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.BlockingVariable
import wordpackbot.dao.StubPlaybackSourceDao

class StubPlaybackSourceDaoTest extends Specification {

    @Shared
            config = new ConfigSlurper().parse('''
wordPacks {
    '188589442' {
        'тест' {
            content = [
                    ['птичка', 'birdy', "'бёди"],
                    ['киска', 'kitty', "'кити"],
                    ['собачка', 'doggy', "'доги"]
            ]
        }
    }
}''')

    @Shared
            dao = new StubPlaybackSourceDao(config, new Random(0))

    @Unroll
    def 'shuffled() returns (pseudo)random sequence each time'() {
        given:
        def result = new BlockingVariable<List>()
        expect:
        dao.shuffled(188589442L, 'тест').whenComplete { res, ex -> result.set(res as List) }
        result.get() == expected
        where:
        expected << [[
                             ['собачка', 'doggy', "'доги"],
                             ['киска', 'kitty', "'кити"],
                             ['птичка', 'birdy', "'бёди"]
                     ], [
                             ['птичка', 'birdy', "'бёди"],
                             ['собачка', 'doggy', "'доги"],
                             ['киска', 'kitty', "'кити"]
                     ], [
                             ['киска', 'kitty', "'кити"],
                             ['птичка', 'birdy', "'бёди"],
                             ['собачка', 'doggy', "'доги"]
                     ]]
    }

}