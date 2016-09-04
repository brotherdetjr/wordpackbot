package wordpackbot.bots;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UpdateEvent {
    private final String text;
    private final Long userId;
    private final Long chatId;
}
