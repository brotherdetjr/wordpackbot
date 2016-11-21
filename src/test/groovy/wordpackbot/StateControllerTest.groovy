package wordpackbot

import groovy.util.logging.Log4j2
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.BlockingVariable
import spock.util.concurrent.BlockingVariables
import wordpackbot.bots.ChatBot
import wordpackbot.bots.UpdateEvent
import wordpackbot.dummy.DummyController
import wordpackbot.dummy.Sender

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

import static com.google.common.collect.Maps.newConcurrentMap
import static com.google.common.util.concurrent.MoreExecutors.directExecutor
import static java.lang.Integer.parseInt
import static java.util.concurrent.CompletableFuture.completedFuture
import static java.util.concurrent.Executors.newFixedThreadPool
import static StateController.NOT_SO_FAST_MESSAGE
import static wordpackbot.VertxUtils.vertxExecutor

@Log4j2
class StateControllerTest extends Specification {

    static final
            USER_1 = 2,
            USER_2 = 22,
            CHAT_1 = 3,
            CHAT_2 = 30,

            EXECUTORS = [
                    fixed1: newFixedThreadPool(1),
                    fixed5: newFixedThreadPool(5),
                    fixed20: newFixedThreadPool(20),
                    vertx: vertxExecutor(),
                    direct: directExecutor()
            ]

    @Unroll
    @SuppressWarnings("GroovyAccessibility")
    def 'DummyController sends incremented state to a proper chat. Executor: #executorName'() {
        given:
        def sender = Mock(Sender)
        def barriers = new BlockingVariables(5)
        def bot = new ChatBot() {
            @Override
            CompletableFuture<?> send(String text, Long chatId) {
                log.debug "Sending '$text' to chat with id $chatId"
                sender.send text, chatId
                barriers.setProperty text, true
                completedFuture null
            }
        }
        new DummyController(bot, newConcurrentMap(), EXECUTORS[executorName], 29).init()
        when:
        bot.fire new UpdateEvent('4', USER_1, CHAT_1)
        barriers.getProperty '2:33'
        then:
        1 * sender.send('2:33', CHAT_1)
        when:
        bot.fire new UpdateEvent('9', USER_2, CHAT_1)
        barriers.getProperty '22:38'
        then:
        1 * sender.send('22:38', CHAT_1)
        when:
        bot.fire new UpdateEvent('9', USER_1, CHAT_1)
        barriers.getProperty '2:42'
        then:
        1 * sender.send('2:42', CHAT_1)
        when:
        bot.fire new UpdateEvent('2', USER_2, CHAT_2)
        barriers.getProperty '22:40'
        then:
        1 * sender.send('22:40', CHAT_2)
        where:
        executorName << EXECUTORS.keySet()
    }

    @Unroll
    @SuppressWarnings("GroovyAccessibility")
    def '"not so fast" message is sent when previous request is not handled yet. Executor: #executorName'() {
        given:
        def barriers = new BlockingVariables(5)
        def serviceBarrier = new BlockingVariable<Boolean>(5)
        def service = new LongRunningService(newFixedThreadPool(1), serviceBarrier)
        def sender = Mock(Sender)
        def bot = new ChatBot() {
            @Override
            CompletableFuture<?> send(String text, Long chatId) {
                log.debug "Sending '$text' to chat with id $chatId"
                sender.send text, chatId
                if (text == NOT_SO_FAST_MESSAGE) {
                    barriers.setProperty 'first', true
                } else if (text == '2:31') {
                    barriers.setProperty 'second', true
                }
                completedFuture null
            }
        }
        new DummyController(bot, newConcurrentMap(), EXECUTORS[executorName], 29) {
            @Override
            protected CompletableFuture<Integer> onUpdate(UpdateEvent event, Integer state) {
                service.sum state, parseInt(event.text)
            }
        }.init()
        when:
        bot.fire new UpdateEvent('2', USER_1, CHAT_1)
        bot.fire new UpdateEvent('3', USER_1, CHAT_1)
        barriers.getProperty 'first'
        then:
        1 * sender.send(NOT_SO_FAST_MESSAGE, CHAT_1)
        when:
        serviceBarrier.set true
        barriers.getProperty 'second'
        then:
        1 * sender.send('2:31', CHAT_1)
        where:
        executorName << EXECUTORS.keySet()
    }

    class LongRunningService {

        private final Executor executor
        private final BlockingVariable<Boolean> barrier

        LongRunningService(Executor executor, BlockingVariable<Boolean> barrier) {
            this.executor = executor
            this.barrier = barrier
        }

        CompletableFuture<Integer> sum(Integer a, Integer b) {
            def future = new CompletableFuture<Integer>()
            executor.execute {
                barrier.get()
                future.complete a + b
            }
            future
        }
    }
}