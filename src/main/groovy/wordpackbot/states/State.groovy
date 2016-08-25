package wordpackbot.states

import io.vertx.groovy.core.Future

interface State<T> {
    def T getValue()
    def Future<State> transit(transition)
}
