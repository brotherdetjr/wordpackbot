package wordpackbot;

import io.vertx.core.Vertx;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Executor;

import static io.vertx.core.Vertx.vertx;

@UtilityClass
public class VertxUtils {

    public static Executor vertxExecutor(Vertx vertx) {
        return command -> vertx.getOrCreateContext().runOnContext(event -> command.run());
    }

    public static Executor vertxExecutor() {
        return vertxExecutor(vertx());
    }
}
