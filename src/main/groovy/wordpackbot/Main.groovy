package wordpackbot

import groovy.util.logging.Log4j2
import org.telegram.telegrambots.TelegramBotsApi
import wordpackbot.bots.TelegramBot
import wordpackbot.dao.StubPlaybackSourceDao
import wordpackbot.states.Playback
import wordpackbot.states.StateFactory

import static java.lang.Thread.currentThread
import static wordpackbot.VertxUtils.vertxExecutor
import static wordpackbot.ViewNameAndState.vs

@Log4j2
class Main {
	static void main(String... args) {
		def config = new ConfigSlurper()
			.parse(currentThread().contextClassLoader.getResourceAsStream('config.groovy').text)
		def stateFactory = new StateFactory(new StubPlaybackSourceDao(config))
		//noinspection GroovyAssignabilityCheck
		def bot = new TelegramBot(config.token, config.name).register(new TelegramBotsApi())
		new Mvc.Builder(bot)
			.initial(Playback, { stateFactory.startPlayback it.userId, 'тест' })
			.controller(
				Playback,
				{ event, playback ->
					(++playback).thenApply { vs Playback, it }
				}
			)
			.view(Playback, { it.send it.newState.value })
			.executor(vertxExecutor())
			.build()
		log.info 'Started'
	}
}