package wordpackbot.bots

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import io.vertx.groovy.core.Future

abstract class Bot {

    private final EventBus eventBus = new EventBus()

    Bot onUpdate(Closure handler) {
        eventBus.register new Object() {
            @SuppressWarnings("GroovyUnusedDeclaration")
            @Subscribe
            void handle(UpdateEvent update) { handler.call update }
        }
        this
    }

    void fire(event) { eventBus.post event }

    abstract Future<Object> send(String text, Long chatId)
}