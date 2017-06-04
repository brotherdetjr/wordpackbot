package brotherdetjr.wordpackbot

import brotherdetjr.pauline.telegram.TelegramRenderer
import brotherdetjr.pauline.telegram.events.TelegramEvent
import brotherdetjr.pauline.test.EventSourceImpl
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import brotherdetjr.wordpackbot.states.StateFactory

import java.util.concurrent.atomic.AtomicReference

import static brotherdetjr.pauline.telegram.test.TelegramEvents.textMessage
import static java.util.concurrent.CompletableFuture.completedFuture

class FlowTest extends Specification {

	@Shared
		stateFactory = new StateFactory(
			{ long userId, String wordPackName ->
				completedFuture(
					[
						['собачка', 'doggy', "'доги"],
						['киска', 'kitty', "'кити"],
						['птичка', 'birdy']
					]
				)
			},
			new Random(0)
		)

	@Shared
		renderer = new AtomicReference<TelegramRenderer>()

	@Shared
		eventSource = new EventSourceImpl<TelegramEvent>()

	@SuppressWarnings("GroovyUnusedDeclaration")
	@Shared
		flow = new FlowFactory(stateFactory).create()
			.eventSource(eventSource).rendererFactory({ renderer.get() }).build()

	@Unroll
	def 'next word is #expected'() {
		given:
		renderer.set Mock(TelegramRenderer)
		when:
		eventSource.fire textMessage('whatever')
		then:
		1 * renderer.get().send(expected)
		where:
		expected << [
			'птичка', 'birdy',
			'киска', 'kitty', "'кити",
			'собачка', 'doggy', "'доги",
			'собачка', 'doggy', "'доги",
			'птичка', 'birdy',
			'киска', 'kitty', "'кити"
		]
	}
}