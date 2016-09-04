import io.vertx.core.Handler
import io.vertx.groovy.core.Context
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import spock.lang.Shared
import spock.lang.Specification
import wordpackbot.bots.ChatBot
import wordpackbot.bots.UpdateEvent
import wordpackbot.dummy.DummyController

import java.util.concurrent.CompletableFuture

import static io.vertx.groovy.core.Future.succeededFuture
import static java.util.concurrent.CompletableFuture.completedFuture

class VertxControllerTest extends Specification {

    static final
            USER_1 = 2,
            USER_2 = 22,
            CHAT_1 = 3,
            CHAT_2 = 30

    @Shared
        vertx = Mock(Vertx) {
            getOrCreateContext() >> Mock(Context) {
                runOnContext(_ as Handler<Void>) >> { Handler<Void> handler -> handler.handle null }
            }
        }

    def 'DummyController sends incremented state to a proper chat'() {
        given:
        def sender = Mock(Sender)
        def bot = new ChatBot() {
            @Override
            CompletableFuture<Object> send(String text, Long chatId) {
                sender.send text, chatId
                completedFuture null
            }
        }
        new DummyController(bot, [:], 29).init vertx
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

/*
    def 'sends "Not So Fast" message if a new update came before the previous one have been processed'() {
        given:
        new DummyController(bot, [:], 29) {
            @Override
            protected Future<Integer> onUpdate(UpdateEvent event, State<Integer> state) {
                if (event.text == '4') {
                    bot.fire new UpdateEvent('2', USER_1, CHAT_1)
                }
                super.onUpdate(event, state)
            }
        }.init vertx
        when:
        bot.fire new UpdateEvent('4', USER_1, CHAT_1)
        then:
        1 * sender.send(NOT_SO_FAST_MESSAGE, CHAT_1)
        1 * sender.send('33', CHAT_1)
    }
*/

    interface Sender {
        void send(String text, Long chatId)
    }
}