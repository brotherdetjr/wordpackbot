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
        bot.fire new UpdateEvent('4', 2, 3)
        then:
        1 * sender.send('33', 3)
    }

    interface Sender {
        void send(String text, Long chatId)
    }
}