package wordpackbot.bots;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class UpdateEvent {
    private final String text;
    private final Long userId;
    private final Long chatId;
}
