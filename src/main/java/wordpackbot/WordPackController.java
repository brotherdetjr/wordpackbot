package wordpackbot;

import wordpackbot.bots.ChatBot;
import wordpackbot.bots.UpdateEvent;
import wordpackbot.states.Playback;
import wordpackbot.states.StateFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class WordPackController extends StateControllerBase<String, String, Playback> {

    private final StateFactory stateFactory;

    public WordPackController(ChatBot bot,
                              Map<Long, Session<String, String, Playback>> sessions,
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
    protected CompletableFuture<String> onUpdate(UpdateEvent event, Playback state) {
        send(state.getValue(), event.getChatId());
        return completedFuture("next");
    }
}
