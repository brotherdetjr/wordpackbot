package wordpackbot

import groovy.util.logging.Log4j2
import spock.lang.Shared
import spock.lang.Specification
import wordpackbot.bots.ChatBot
import wordpackbot.bots.UpdateEvent
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.dummy.Sender
import wordpackbot.states.StateFactory

import java.util.concurrent.CompletableFuture

import static com.google.common.collect.Maps.newConcurrentMap
import static com.google.common.util.concurrent.MoreExecutors.directExecutor
import static java.util.concurrent.CompletableFuture.completedFuture
import static wordpackbot.dummy.TestUtils.blockingVar

@Log4j2
class WordPackControllerTest extends Specification {

    static final
            USER_1 = 2,
            USER_2 = 22,
            CHAT_1 = 3,
            CHAT_2 = 30

    @Shared
            config = new ConfigSlurper().parse('''
wordPacks {
    '2' {
        'тест' {
            content = [
                    ['птичка', 'birdy'],
                    ['киска', 'pussy', "'паси"],
                    ['собачка', 'doggy', "'доги"]
            ]
        }
    }
    '22' {
        'тест' {
            content = [
                    ['осёл', 'ass', "эсс"],
            ]
        }
    }
}''')

    @Shared
            stateFactory = new StateFactory(new StubPlaybackSourceDao(config, new Random(0)))

    @Shared
            results = [
                    'осёл'   : blockingVar(), 'ass': blockingVar(), "эсс": blockingVar(),
                    'собачка': blockingVar(), 'doggy': blockingVar(), "'доги": blockingVar(),
                    'киска'  : blockingVar(), 'pussy': blockingVar(), "'паси": blockingVar(),
                    'птичка' : blockingVar(), 'birdy': blockingVar()
            ]

    @SuppressWarnings("GroovyAccessibility")
    def 'WordPackController sends proper words.'() {
        given:
        def sender = Mock(Sender)
        def bot = new ChatBot() {
            @Override
            CompletableFuture<?> send(String text, Long chatId) {
                log.debug "Sending '$text' to chat with id $chatId"
                sender.send text, chatId
                results[text].set true
                completedFuture null
            }
        }
        new WordPackController(bot, newConcurrentMap(), directExecutor(), stateFactory).init()
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor 'собачка'
        then:
        1 * sender.send('собачка', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_2, CHAT_2)
        waitFor 'осёл'
        then:
        1 * sender.send('осёл', CHAT_2)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor 'doggy'
        then:
        1 * sender.send('doggy', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor "'доги"
        then:
        1 * sender.send("'доги", CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor 'киска'
        then:
        1 * sender.send('киска', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor 'pussy'
        then:
        1 * sender.send('pussy', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor "'паси"
        then:
        1 * sender.send("'паси", CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor 'птичка'
        then:
        1 * sender.send('птичка', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor 'birdy'
        then:
        1 * sender.send('birdy', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_2, CHAT_2)
        waitFor 'ass'
        then:
        1 * sender.send('ass', CHAT_2)
        when:
        bot.fire new UpdateEvent('whatever', USER_2, CHAT_2)
        waitFor 'эсс'
        then:
        1 * sender.send('эсс', CHAT_2)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor 'птичка'
        then:
        1 * sender.send('птичка', CHAT_1)
        when:
        bot.fire new UpdateEvent('whatever', USER_1, CHAT_1)
        waitFor 'birdy'
        then:
        1 * sender.send('birdy', CHAT_1)
    }

    def waitFor(String key) {
        results[key].get()
        results[key] = blockingVar()
    }
}