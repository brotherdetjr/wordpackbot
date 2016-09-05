package wordpackbot

import groovy.util.logging.Log4j2
import org.telegram.telegrambots.TelegramBotsApi
import wordpackbot.bots.TelegramBot
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.StateFactory

import static java.lang.Thread.currentThread
import static wordpackbot.VertxUtils.vertxExecutor

@Log4j2
class Main {
    static void main(String ... args) {
        def config = new ConfigSlurper()
                .parse(currentThread().contextClassLoader.getResourceAsStream('config.groovy').text)
        def stateFactory = new StateFactory(new StubPlaybackSourceDao(config))
        //noinspection GroovyAssignabilityCheck
        def bot = new TelegramBot(config.token, config.name).register(new TelegramBotsApi())
        new WordPackController(bot, [:], vertxExecutor(), stateFactory).init()
        log.info 'Started'
    }
}