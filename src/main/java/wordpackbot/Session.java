package wordpackbot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Session<S> {
    private volatile S state;
    private volatile boolean busy;
}
