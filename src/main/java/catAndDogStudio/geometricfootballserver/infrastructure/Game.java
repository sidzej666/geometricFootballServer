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
    private SelectableChannel hostChannel;
    private String preferredColor;
    private String grantedColor;
    private String tactic;
    private String tacticMapping;
    private String players;
    private String team;
    private Map<String, String> playersUsersMapping = new HashMap<>();
    private String partialMessage;

    public void setUp(String ownerName) {
        this.ownerName = ownerName;
        this.lastHearthBeatTime = new Date().getTime();
    }

    public Game() {

    }
}
