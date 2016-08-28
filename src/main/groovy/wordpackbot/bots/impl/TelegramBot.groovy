package wordpackbot.bots.impl

import io.vertx.groovy.core.Future
import org.json.JSONObject
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.api.methods.BotApiMethod
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.api.objects.Update as TelegramUpdate
import org.telegram.telegrambots.updateshandlers.SentCallback
import wordpackbot.bots.Bot
import wordpackbot.bots.BotException
import wordpackbot.bots.UpdateEvent

import static UpdateUtils.extractChatId
import static UpdateUtils.extractText
import static UpdateUtils.extractUserId
import static io.vertx.groovy.core.Future.future

class TelegramBot extends Bot {

    private final TelegramLongPollingBot bot

    TelegramBot(String token, String name) {
        bot = new TelegramLongPollingBot() {
            @Override
            String getBotToken() { token }

            @Override
            void onUpdateReceived(TelegramUpdate update) {
                fire new UpdateEvent(extractText(update), extractUserId(update), extractChatId(update))
            }

            @Override
            String getBotUsername() { name }
        }
    }

    TelegramBot register(TelegramBotsApi api) {
        api.registerBot bot
        this
    }

    @Override
    Future<Object> send(String text, Long chatId) {
        def future = future()
        bot.sendMessageAsync new SendMessage(text: text, chatId: Long.toString(chatId)), new SentCallback<Message>() {
            @Override
            void onResult(BotApiMethod<Message> method, JSONObject jsonObject) {
                future.complete jsonObject
            }

            @Override
            void onError(BotApiMethod<Message> method, JSONObject jsonObject) {
                future.fail new BotException(jsonObject)
            }

            @Override
            void onException(BotApiMethod<Message> method, Exception exception) {
                future.fail new BotException(null, exception)
            }
        }
        future
    }
}
