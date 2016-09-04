package wordpackbot.dummy

import groovy.util.logging.Log4j2
import wordpackbot.Session
import wordpackbot.VertxController
import wordpackbot.bots.ChatBot
import wordpackbot.bots.UpdateEvent

import java.util.concurrent.CompletableFuture

import static java.util.concurrent.CompletableFuture.completedFuture
import static wordpackbot.dummy.DummyState.futureDummyState

@Log4j2
class DummyController extends VertxController<Integer, String, DummyState> {

    private final int initialValue

    DummyController(ChatBot bot, Map<Long, Session> sessions, int initialValue) {
        super(bot, sessions)
        this.initialValue = initialValue
    }

    @Override
    protected CompletableFuture<DummyState> initialState(long userId) { futureDummyState initialValue }

    @Override
    protected CompletableFuture<String> onUpdate(UpdateEvent event, DummyState state) {
        def increment = Integer.parseInt(event.text)
        send Integer.toString(state.value + increment), event.chatId
        completedFuture increment
    }
}
