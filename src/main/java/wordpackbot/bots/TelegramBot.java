package wordpackbot.bots;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.updateshandlers.SentCallback;

import java.util.concurrent.CompletableFuture;

import static wordpackbot.bots.UpdateUtils.extractChatId;
import static wordpackbot.bots.UpdateUtils.extractText;
import static wordpackbot.bots.UpdateUtils.extractUserId;

@Log4j2
public class TelegramBot extends ChatBot {

    private final TelegramLongPollingBot bot;

    public TelegramBot(String token, String name) {
        bot = new TelegramLongPollingBot() {
            @Override
            public String getBotToken() { return token; }

            @Override
            public void onUpdateReceived(Update update) {
                fire(new UpdateEvent(extractText(update), extractUserId(update), extractChatId(update)));
            }

            @Override
            public String getBotUsername() { return name; }
        };
    }

    @SneakyThrows(TelegramApiException.class)
    public TelegramBot register(TelegramBotsApi api) {
        api.registerBot(bot);
        return this;
    }

    @SneakyThrows(TelegramApiException.class)
    @Override
    public CompletableFuture<?> send(String text, Long chatId) {
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(Long.toString(chatId));
        log.debug("Sending '{}' to chat with id {}", text, chatId);
        bot.sendMessageAsync(message, new SentCallback<Message>() {
            @Override
            public void onResult(BotApiMethod<Message> method, JSONObject jsonObject) {
                future.complete(jsonObject);
            }

            @Override
            public void onError(BotApiMethod<Message> method, JSONObject jsonObject) {
                future.completeExceptionally(new BotException(jsonObject));
            }

            @Override
            public void onException(BotApiMethod<Message> method, Exception exception) {
                future.completeExceptionally(new BotException(null, exception));
            }
        });
        return future;
    }
}
