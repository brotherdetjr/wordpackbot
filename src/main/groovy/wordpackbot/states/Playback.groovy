package wordpackbot.states

import io.vertx.groovy.core.Future

import static io.vertx.groovy.core.Future.succeededFuture

class Playback implements State<String> {

    final static END_OF_ENTRY = new String()

    private final String value
    private final Iterator<String> entryIterator
    private final Closure<Future<State>> onFinish

    Playback(String value, Iterator entryIterator, Closure<Future<State>> onFinish) {
        this.value = value
        this.entryIterator = entryIterator
        this.onFinish = onFinish
    }

    @Override
    String getValue() { value }

    @SuppressWarnings("ChangeToOperator")
    @Override
    Future<Playback> transit(transition) {
        def value = entryIterator.next()
        if (value == END_OF_ENTRY) {
            value = entryIterator.hasNext() ? entryIterator.next() : null
        }
        value ? succeededFuture(new Playback(value, entryIterator, onFinish) as State) : onFinish() as Future<Playback>
    }
}
