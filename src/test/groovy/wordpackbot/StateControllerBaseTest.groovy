package wordpackbot

import groovy.util.logging.Log4j2
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable
import wordpackbot.bots.ChatBot
import wordpackbot.bots.UpdateEvent
import wordpackbot.dummy.DummyController

import java.util.concurrent.CompletableFuture

import static com.google.common.collect.Maps.newConcurrentMap
import static java.util.concurrent.CompletableFuture.completedFuture
import static java.util.concurrent.Executors.newFixedThreadPool

@Log4j2
class StateControllerBaseTest extends Specification {

    static final
            USER_1 = 2,
            USER_2 = 22,
            CHAT_1 = 3,
            CHAT_2 = 30

    @SuppressWarnings("GroovyAccessibility")
    def 'DummyController sends incremented state to a proper chat'() {
        given:
        def sender = Mock(Sender)
        def results = [
                '2:33' : new BlockingVariable<Boolean>(),
                '22:38': new BlockingVariable<Boolean>(),
                '2:42' : new BlockingVariable<Boolean>(),
                '22:40': new BlockingVariable<Boolean>()
        ]
        def bot = new ChatBot() {
            @Override
            CompletableFuture<?> send(String text, Long chatId) {
                log.debug "Sending '$text' to chat with id $chatId"
                sender.send text, chatId
                results[text].set true
                completedFuture null
            }
        }
        new DummyController(bot, newConcurrentMap(), newFixedThreadPool(5), 29).init()
        when:
        bot.fire new UpdateEvent('4', USER_1, CHAT_1)
        results['2:33'].get()
        then:
        1 * sender.send('2:33', CHAT_1)
        when:
        bot.fire new UpdateEvent('9', USER_2, CHAT_1)
        results['22:38'].get()
        then:
        1 * sender.send('22:38', CHAT_1)
        when:
        bot.fire new UpdateEvent('9', USER_1, CHAT_1)
        results['2:42'].get()
        then:
        1 * sender.send('2:42', CHAT_1)
        when:
        bot.fire new UpdateEvent('2', USER_2, CHAT_2)
        results['22:40'].get()
        then:
        1 * sender.send('22:40', CHAT_2)
    }

    interface Sender {
        void send(String text, Long chatId)
    }
}