package wordpackbot

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.BlockingVariable
import wordpackbot.dao.StubPlaybackSourceDao

class StubPlaybackSourceDaoTest extends Specification {

	@Shared
		config = new ConfigSlurper().parse('''
			|wordPacks {
			|	'188589442' {
			|		'тест' {
			|			content = [
			|				['птичка', 'birdy', "'бёди"],
			|				['киска', 'kitty', "'кити"],
			|				['собачка', 'doggy', "'доги"]
			|			]
			|		}
			|	}
			|}'''.stripMargin())

	@Shared
		dao = new StubPlaybackSourceDao(config)

	@Unroll
	def 'getWorkPack() returns preconfigured list of lists of strings for now'() {
		given:
		def result = new BlockingVariable<List>()
		dao.getWordPack(188589442L, 'тест').whenComplete { res, ex -> result.set(res as List) }
		expect:
		result.get() == [
			['птичка', 'birdy', "'бёди"],
			['киска', 'kitty', "'кити"],
			['собачка', 'doggy', "'доги"]
		]
	}

}