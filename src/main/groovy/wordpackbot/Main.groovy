package wordpackbot

import groovy.util.logging.Log4j2
import org.telegram.telegrambots.TelegramBotsApi
import wordpackbot.bots.impl.TelegramBot
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.StateFactory

import static io.vertx.groovy.core.Vertx.vertx
import static java.lang.Thread.currentThread

@Log4j2
class Main {
    static void main(String ... args) {
        def config = new ConfigSlurper()
                .parse(currentThread().contextClassLoader.getResourceAsStream('config.groovy').text)
        def stateFactory = new StateFactory(new StubPlaybackSourceDao(config))
        //noinspection GroovyAssignabilityCheck
        def bot = new TelegramBot(config.token, config.name).register(new TelegramBotsApi())
        new WordPackController(vertx(), bot, stateFactory)
        log.info 'Started'
    }
}