import dummy.DummyController
import io.vertx.core.Handler
import io.vertx.groovy.core.Context
import io.vertx.groovy.core.Future
import io.vertx.groovy.core.Vertx
import spock.lang.Specification
import wordpackbot.bots.Bot
import wordpackbot.bots.UpdateEvent

import static io.vertx.groovy.core.Future.succeededFuture

class VertxControllerTest extends Specification {

    static final
            USER_1 = 2,
            USER_2 = 22,
            CHAT_1 = 3,
            CHAT_2 = 30

    def 'does my day'() {
        given:
        def vertx = Mock(Vertx) {
            getOrCreateContext() >> Mock(Context) {
                runOnContext(_ as Handler<Void>) >> { Handler<Void> handler -> handler.handle null }
            }
        }
        def sender = Mock(Sender)
        def bot = new Bot() {
            @Override
            Future<Object> send(String text, Long chatId) {
                sender.send text, chatId
                succeededFuture null
            }
        }
        //noinspection GroovyResultOfObjectAllocationIgnored
        new DummyController(vertx, bot, 29)
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