package wordpackbot.dummy

import groovy.util.logging.Log4j2
import wordpackbot.Session
import wordpackbot.StateControllerBase
import wordpackbot.bots.ChatBot
import wordpackbot.bots.UpdateEvent

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

import static java.util.concurrent.CompletableFuture.completedFuture
import static wordpackbot.dummy.DummyState.futureDummyState

@Log4j2
class DummyController extends StateControllerBase<Integer, Integer, DummyState> {

    private final int initialValue

    DummyController(ChatBot bot, Map<Long, Session> sessions, Executor executor, int initialValue) {
        super(bot, sessions, executor)
        this.initialValue = initialValue
    }

    @Override
    protected CompletableFuture<DummyState> initialState(long userId) {
        futureDummyState initialValue
    }

    @Override
    protected CompletableFuture<Integer> onUpdate(UpdateEvent event, DummyState state) {
        def increment = Integer.parseInt(event.text)
        send "$event.userId:${state.value + increment}", event.chatId
        completedFuture increment
    }
}
