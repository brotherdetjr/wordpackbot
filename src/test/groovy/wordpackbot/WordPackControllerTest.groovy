package wordpackbot

import groovy.util.logging.Log4j2
import spock.lang.Specification
import spock.util.concurrent.BlockingVariables
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.StateFactory

import java.util.concurrent.CompletableFuture

import static com.google.common.collect.Maps.newConcurrentMap
import static com.google.common.util.concurrent.MoreExecutors.directExecutor
import static java.util.concurrent.CompletableFuture.completedFuture

@Log4j2
class WordPackControllerTest extends Specification {

/*    static final
            USER_1 = 2,
            USER_2 = 22,
            CHAT_1 = 3,
            CHAT_2 = 30

    @SuppressWarnings("GroovyAccessibility")
    def 'WordPackController sends proper words.'() {
        given:
        def config = new ConfigSlurper().parse('''
wordPacks {
    '2' {
        'тест' {
            content = [
                    ['птичка', 'birdy'],
                    ['киска', 'kitty', "'кити"],
                    ['собачка', 'doggy', "'доги"]
            ]
        }
    }
    '22' {
        'тест' {
            content = [
                    ['осёл', 'donkey', "'данки"],
            ]
        }
    }
}''')
        def stateFactory = new StateFactory(new StubPlaybackSourceDao(config, new Random(0)))
        def barriers = new BlockingVariables(5)
        def sender = Mock(Sender)
        def bot = new ChatBot() {
            @Override
            CompletableFuture<?> send(String text, Long chatId) {
                log.debug "Sending '$text' to chat with id $chatId"
                sender.send text, chatId
                barriers.setProperty text, true
                completedFuture null
            }
        }
        new WordPackController(bot, newConcurrentMap(), directExecutor(), stateFactory).init()
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty 'собачка'
        then:
        1 * sender.send('собачка', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_2, CHAT_2)
        barriers.getProperty 'осёл'
        then:
        1 * sender.send('осёл', CHAT_2)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty 'doggy'
        then:
        1 * sender.send('doggy', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty "'доги"
        then:
        1 * sender.send("'доги", CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty 'киска'
        then:
        1 * sender.send('киска', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty 'kitty'
        then:
        1 * sender.send('kitty', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty "'кити"
        then:
        1 * sender.send("'кити", CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty 'птичка'
        then:
        1 * sender.send('птичка', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty 'birdy'
        then:
        1 * sender.send('birdy', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_2, CHAT_2)
        barriers.getProperty 'donkey'
        then:
        1 * sender.send('donkey', CHAT_2)
        when:
        bot.fire new UpdateEvent('whatever', USER_2, CHAT_2)
        barriers.getProperty "'данки"
        then:
        1 * sender.send("'данки", CHAT_2)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty 'птичка'
        then:
        1 * sender.send('птичка', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        barriers.getProperty 'birdy'
        then:
        1 * sender.send('birdy', CHAT_1)
    }*/
}