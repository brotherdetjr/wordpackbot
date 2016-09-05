package wordpackbot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import wordpackbot.states.State;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Session<V, T, S extends State<V, T, S>> {
    private volatile S state;
    private volatile boolean busy;
}
