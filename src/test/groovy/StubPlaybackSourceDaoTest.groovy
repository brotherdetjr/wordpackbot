import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.BlockingVariable
import wordpackbot.dao.StubPlaybackSourceDao

class StubPlaybackSourceDaoTest extends Specification {

    @Shared
            random = new Random(0)

    @Shared
            config = new ConfigSlurper().parse('''
wordPacks {
    '188589442' {
        'тест' {
            content = [
                    ['птичка', 'birdy', "'бёди"],
                    ['киска', 'pussy', "'паси"],
                    ['собачка', 'doggy', "'доги"]
            ]
        }
    }
}''')

    @Shared
            dao = new StubPlaybackSourceDao(config, random)

    @Unroll
    def 'shuffled() returns (pseudo)random sequence each time'() {
        given:
        def result = new BlockingVariable<Boolean>()
        expect:
        dao.shuffled(188589442L, 'тест').setHandler { result.set(it.result() == expected) }
        result.get()
        where:
        expected << [[
                             ['собачка', 'doggy', "'доги"],
                             ['киска', 'pussy', "'паси"],
                             ['птичка', 'birdy', "'бёди"]
                     ], [
                             ['птичка', 'birdy', "'бёди"],
                             ['собачка', 'doggy', "'доги"],
                             ['киска', 'pussy', "'паси"]
                     ], [
                             ['киска', 'pussy', "'паси"],
                             ['птичка', 'birdy', "'бёди"],
                             ['собачка', 'doggy', "'доги"]
                     ]]
    }

}