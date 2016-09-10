package wordpackbot.dummy

import spock.util.concurrent.BlockingVariable

import static java.util.concurrent.TimeUnit.SECONDS

class TestUtils {

    private TestUtils() {
        throw new AssertionError()
    }

    def static BlockingVariable<Boolean> blockingVar() {
        new BlockingVariable<Boolean>(5, SECONDS)
    }
}
