package wordpackbot.states

import io.vertx.groovy.core.Future

interface State {
    def <T> T getValue()
    def Future<State> transit(transition)
}
