package wordpackbot.bots;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Log4j2
public abstract class ChatBot {

    private final EventBus eventBus = new EventBus();

    public ChatBot onUpdate(Consumer<UpdateEvent> consumer) {
        eventBus.register(new Object() {
            @SuppressWarnings("unused")
            @Subscribe
            void handle(UpdateEvent event) {
                consumer.accept(event);
            }
        });
        return this;
    }

    protected void fire(Object event) {
        log.debug("Firing event: {}", event);
        eventBus.post(event);
    }

    public abstract CompletableFuture<?> send(String text, Long chatId);
}
