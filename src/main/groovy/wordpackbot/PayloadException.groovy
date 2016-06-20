package wordpackbot

class PayloadException<T> extends RuntimeException {
    private final T payload

    PayloadException(T payload) {
        this.payload = payload
    }

    T getPayload() { payload }
}
