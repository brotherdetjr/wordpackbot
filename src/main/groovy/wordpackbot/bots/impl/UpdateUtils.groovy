package wordpackbot.bots.impl

import org.telegram.telegrambots.api.objects.Update

class UpdateUtils {

    private UpdateUtils() {
        throw new AssertionError()
    }

    static String extractText(Update update) {
        if (update.hasMessage()) {
            return update.message.text
        } else {
            throw new IllegalArgumentException()
        }
    }

    static Long extractUserId(Update update) {
        if (update.hasMessage()) {
            return update.message.from.id
        } else if (update.hasInlineQuery()) {
            return update.inlineQuery.from.id
        } else if (update.hasCallbackQuery()) {
            return update.callbackQuery.from.id
        } else if (update.hasChosenInlineQuery()) {
            return update.chosenInlineQuery.from.id
        } else if (update.hasEditedMessage()) {
            return update.editedMessage.from.id
        } else {
            throw new IllegalArgumentException()
        }
    }

    static Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.message.chatId
        } else if (update.hasEditedMessage()) {
            return update.editedMessage.chatId
        } else {
            throw new IllegalArgumentException()
        }
    }

}
