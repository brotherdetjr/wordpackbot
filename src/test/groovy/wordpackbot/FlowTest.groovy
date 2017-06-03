package wordpackbot

import brotherdetjr.pauline.telegram.TelegramRenderer
import brotherdetjr.pauline.telegram.events.TelegramEvent
import brotherdetjr.pauline.telegram.events.TextMessageEvent
import brotherdetjr.pauline.test.EventSourceImpl
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import wordpackbot.states.StateFactory

import java.util.concurrent.atomic.AtomicReference

import static java.util.concurrent.CompletableFuture.completedFuture

class FlowTest extends Specification {

	@Shared
		stateFactory = new StateFactory(
			{ long userId, String wordPackName -> /* not really shuffled */
				completedFuture(
					[
						['собачка', 'doggy', "'доги"],
						['киска', 'kitty', "'кити"],
						['птичка', 'birdy']
					]
				)
			}
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
		eventSource.fire(new TextMessageEvent(3L, 'disa', 4L, 5L, 'whatever'))
		then:
		1 * renderer.get().send(expected)
		where:
		expected << ['собачка', 'doggy', "'доги",
					 'киска', 'kitty', "'кити",
					 'птичка', 'birdy',
					 'собачка', 'doggy', "'доги",
					 'киска', 'kitty', "'кити",
					 'птичка', 'birdy']
	}
}