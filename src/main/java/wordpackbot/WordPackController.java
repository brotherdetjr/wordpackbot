package wordpackbot;

import wordpackbot.bots.ChatBot;
import wordpackbot.bots.UpdateEvent;
import wordpackbot.states.Playback;
import wordpackbot.states.StateFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class WordPackController extends StateControllerBase<Playback> {

    private final StateFactory stateFactory;

    public WordPackController(ChatBot bot,
                              ConcurrentMap<Long, Session<Playback>> sessions,
                              Executor executor,
                              StateFactory stateFactory) {
        super(bot, sessions, executor);
        this.stateFactory = stateFactory;
    }

    @Override
    protected CompletableFuture<Playback> initialState(long userId) {
        return stateFactory.startPlayback(userId, "тест");
    }

    @Override
    protected CompletableFuture<Playback> onUpdate(UpdateEvent event, Playback state) {
        return state.next();
    }

    @Override
    protected void afterTransition(AfterTransitionContext<Playback> context) {
        context.send(context.getNewState().getValue(), context.getEvent().getChatId());
    }
}
