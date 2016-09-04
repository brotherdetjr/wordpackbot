package wordpackbot.bots;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.api.objects.Update;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class UpdateUtils {

    public static String extractText(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getText();
        } else {
            throw new BotException(update);
        }
    }

    public static Long extractUserId(Update update) {
        if (update.hasMessage()) {
            return Long.valueOf(update.getMessage().getFrom().getId());
        } else {
            throw new BotException(update);
        }
    }

    public static Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else {
            throw new BotException(update);
        }
    }
}
