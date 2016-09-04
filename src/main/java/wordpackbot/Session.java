package wordpackbot;

import lombok.Getter;
import lombok.Setter;
import wordpackbot.states.State;

@Getter
@Setter
public class Session<V, T, S extends State<V, T, S>> {
    private boolean busy;
    private S state;

    public Session(S initial) {
        this.state = initial;
    }
}
