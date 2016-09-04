package wordpackbot;

import io.vertx.core.Vertx;
import lombok.NoArgsConstructor;

import java.util.concurrent.Executor;

import static io.vertx.core.Vertx.vertx;

@NoArgsConstructor
public class VertxUtils {

    public static Executor vertxExecutor(Vertx vertx) {
        return command -> vertx.getOrCreateContext().runOnContext(event -> command.run());
    }

    public static Executor vertxExecutor() {
        return vertxExecutor(vertx());
    }
}
