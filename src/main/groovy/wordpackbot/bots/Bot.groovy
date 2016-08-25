package wordpackbot.bots

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe

abstract class Bot {

    private final EventBus eventBus = new EventBus()

    Bot onUpdate(Closure handler) {
        eventBus.register new Object() {
            @Subscribe
            void handle(UpdateEvent update) { handler.call update }
        }
        this
    }

    Bot onError(Closure handler) {
        eventBus.register new Object() {
            @Subscribe
            void handle(Throwable t) { handler.call t }
        }
        this
    }

    protected void fire(event) { eventBus.post event }

    abstract void send(String text, Long chatId, Closure callback = { -> })
}