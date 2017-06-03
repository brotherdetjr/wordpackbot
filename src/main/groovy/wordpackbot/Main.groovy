package wordpackbot

import groovy.util.logging.Slf4j
import brotherdetjr.pauline.telegram.TelegramEngine
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.Playback
import wordpackbot.states.StateFactory

import static java.lang.Thread.currentThread
import static brotherdetjr.pauline.telegram.TelegramUtils.extractUserId
import static wordpackbot.VertxUtils.vertxExecutor

@Slf4j
class Main {
	static void main(String... args) {
		def config = new ConfigSlurper()
			.parse(currentThread().contextClassLoader.getResourceAsStream('config.groovy').text)
		def stateFactory = new StateFactory(new StubPlaybackSourceDao(config))
		TelegramEngine.builder(config.bot.token as String, config.bot.name as String)
			.initial({ stateFactory.startPlayback extractUserId(it), 'тест' })
			.handle().when(Playback).by({ event, playback -> ++playback })
			.render(Playback).as({ it.renderer.send it.state.value })
			.executor(vertxExecutor())
			.build()
		log.info 'Started'
	}
}