package brotherdetjr.wordpackbot

import brotherdetjr.pauline.telegram.TelegramFlowConfigurer
import groovy.util.logging.Slf4j
import brotherdetjr.wordpackbot.dao.StubPlaybackSourceDao
import brotherdetjr.wordpackbot.states.StateFactory

import static brotherdetjr.utils.Utils.resourceAsStream
import static brotherdetjr.utils.vertx.VertxUtils.vertxExecutor

@Slf4j
class Main {
	static void main(String... args) {
		def config = new ConfigSlurper().parse(resourceAsStream('config.groovy').text)
		def stateFactory = new StateFactory(new StubPlaybackSourceDao(config))
		def flow = new FlowFactory(stateFactory).create()
		TelegramFlowConfigurer
			.configure(flow, config.bot.token as String, config.bot.name as String)
			.executor(vertxExecutor())
			.build()
		log.info 'Started'
	}
}