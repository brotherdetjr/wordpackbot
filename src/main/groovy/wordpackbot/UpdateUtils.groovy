package wordpackbot

import org.telegram.telegrambots.api.objects.Update

class UpdateUtils {

    private UpdateUtils() {
        // utility class
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
}
