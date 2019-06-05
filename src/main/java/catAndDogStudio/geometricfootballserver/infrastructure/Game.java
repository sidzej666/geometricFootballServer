package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.channels.SelectableChannel;
import java.util.*;

@Getter
@Setter
public class Game {
    private String ownerName;
    private PlayerState playerState = PlayerState.AWAITING_AUTHENTICATION;
    private final List<Invitation> invitations = new ArrayList<>();
    private final Map<SelectableChannel, Game> playersInGame = new HashMap<>();
    private long lastHearthBeatTime;

    public void setUp(String ownerName) {
        this.ownerName = ownerName;
        this.lastHearthBeatTime = new Date().getTime();
    }

    public Game() {

    }
}
