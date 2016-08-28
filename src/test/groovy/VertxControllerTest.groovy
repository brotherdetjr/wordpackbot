import dummy.DummyController
import io.vertx.core.Handler
import io.vertx.groovy.core.Vertx
import spock.lang.Specification
import wordpackbot.bots.Bot
import wordpackbot.bots.UpdateEvent

import static io.vertx.core.Future.succeededFuture

class VertxControllerTest extends Specification {
    def 'does my day'() {
        given:
        def vertx = Mock(Vertx) {
            getOrCreateContext() >> [runOnContext: { Handler<Void> handler -> handler.handle null }]
        }
        def bot = Mock(Bot) {
            //noinspection GroovyAssignabilityCheck
            1 * send('4', 42) >> succeededFuture()
        }
        def controller = new DummyController(vertx, bot)
        when:
        controller.onUpdate(new UpdateEvent('4', 2, 3), )
    }
}