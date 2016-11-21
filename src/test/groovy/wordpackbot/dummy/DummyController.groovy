package wordpackbot.dummy

import groovy.util.logging.Log4j2
import wordpackbot.RenderContext
import wordpackbot.Session
import wordpackbot.StateController
import wordpackbot.bots.ChatBot
import wordpackbot.bots.UpdateEvent

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executor

import static java.lang.Integer.parseInt
import static java.util.concurrent.CompletableFuture.completedFuture

@Log4j2
class DummyController extends StateController<Integer> {

    private final int initialValue

    DummyController(ChatBot bot, ConcurrentMap<Long, Session<Integer>> sessions, Executor executor, int initialValue) {
        super(bot, sessions, executor)
        this.initialValue = initialValue
    }

    @Override
    protected CompletableFuture<Integer> initialState(long userId) {
        completedFuture initialValue
    }

    @Override
    protected CompletableFuture<Integer> onUpdate(UpdateEvent event, Integer state) {
        completedFuture state + parseInt(event.text)
    }

    @Override
    protected void afterTransition(RenderContext<Integer> context) {
        context.send "$context.event.userId:$context.newState", context.event.chatId
    }
}
