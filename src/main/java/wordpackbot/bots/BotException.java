package wordpackbot.bots;

import lombok.Getter;

public class BotException extends RuntimeException {

    @Getter private final Object value;

    public BotException(Object value, Throwable cause) {
        super(cause);
        this.value = value;
    }

    public BotException(Object value) {
        this.value = value;
    }

    public BotException() {
        this.value = null;
    }
}
