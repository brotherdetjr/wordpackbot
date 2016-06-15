package wordpackbot.states

import io.vertx.groovy.core.Future

import static io.vertx.groovy.core.Future.succeededFuture

class Playback implements State {

    final static END_OF_ENTRY = new Object()

    private final String value
    private final Iterator entryIterator
    private final Closure<Future<State>> onFinish

    Playback(String value, Iterator entryIterator, Closure<Future<State>> onFinish) {
        this.value = value
        this.entryIterator = entryIterator
        this.onFinish = onFinish
    }

    @Override
    String getValue() { value }

    @Override
    Future<State> transit(Object transition) {
        def value = entryIterator.next()
        if (value == END_OF_ENTRY) {
            value = entryIterator.hasNext() ? entryIterator.next() : null
        }
        value ? succeededFuture(new Playback(value as String, entryIterator, onFinish) as State) : onFinish()
    }
}
