package wordpackbot

import brotherdetjr.pauline.telegram.TelegramFlowConfigurer
import groovy.util.logging.Slf4j
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.StateFactory

import static java.lang.Thread.currentThread
import static wordpackbot.VertxUtils.vertxExecutor

@Slf4j
class Main {
	static void main(String... args) {
		def config = new ConfigSlurper()
			.parse(currentThread().contextClassLoader.getResourceAsStream('config.groovy').text)
		def stateFactory = new StateFactory(new StubPlaybackSourceDao(config))
		def flow = new FlowFactory(stateFactory).create()
		TelegramFlowConfigurer
			.configure(flow, config.bot.token as String, config.bot.name as String)
			.executor(vertxExecutor())
			.build()
		log.info 'Started'
	}
}