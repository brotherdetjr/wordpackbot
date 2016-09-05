package wordpackbot

import spock.lang.Specification


class RandomTest extends Specification {
    def 'random is not really random'() {
        given:
        def random = new Random(0)
        expect:
        random.nextInt() == -1155484576
        random.nextInt() == -723955400
        random.nextInt() == 1033096058
        random.nextInt() == -1690734402
    }
}