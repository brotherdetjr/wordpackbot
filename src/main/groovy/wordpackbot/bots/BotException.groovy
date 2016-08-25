package wordpackbot.bots

class BotException<T> extends RuntimeException {

    private final T value

    BotException(T value = null, Throwable cause = null) {
        super(cause)
        this.value = value
    }

    T getValue() { value }
}
