import spock.lang.Specification
import wordpackbot.bots.ChatBot
import wordpackbot.bots.UpdateEvent
import wordpackbot.dummy.DummyController

import java.util.concurrent.CompletableFuture

import static java.util.concurrent.CompletableFuture.completedFuture

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
        def bot = new ChatBot() {
            @Override
            CompletableFuture<?> send(String text, Long chatId) {
                sender.send text, chatId
                completedFuture null
            }
        }
        new DummyController(bot, [:], 29).init()
        when:
        bot.fire new UpdateEvent('4', USER_1, CHAT_1)
        then:
        1 * sender.send('33', CHAT_1)
        when:
        bot.fire new UpdateEvent('9', USER_2, CHAT_1)
        then:
        1 * sender.send('38', CHAT_1)
        when:
        bot.fire new UpdateEvent('9', USER_1, CHAT_1)
        then:
        1 * sender.send('42', CHAT_1)
        when:
        bot.fire new UpdateEvent('2', USER_2, CHAT_2)
        then:
        1 * sender.send('40', CHAT_2)
    }

    interface Sender {
        void send(String text, Long chatId)
    }
}