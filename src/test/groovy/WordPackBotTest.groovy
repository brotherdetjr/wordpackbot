import io.vertx.groovy.core.Vertx
import spock.lang.Specification
import wordpackbot.WordPackController
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.StateFactory

import static java.lang.Thread.currentThread


class WordPackBotTest extends Specification {

    def 'does my day'() {
        given:
        def config = new ConfigSlurper()
                .parse(currentThread().contextClassLoader.getResourceAsStream('config.groovy').text)
        def bot = new WordPackController(Mock(Vertx) {
            getOrCreateContext() >> { /* TODO */ }
        }, config, new StateFactory(new StubPlaybackSourceDao(config, new Random(0))))
        expect:
        2 == 2
    }

}