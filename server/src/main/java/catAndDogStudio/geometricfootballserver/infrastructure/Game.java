package catAndDogStudio.geometricfootballserver.infrastructure;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.Getter;
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
    private final Map<ChannelId, Game> playersInTeam = new HashMap<>();
    private long lastHearthBeatTime;
    private Long readyForGameTime;
    private Channel hostChannel;
    private Channel channel;
    private String preferredColor;
    private String grantedColor;
    private String gameName;
    private String waitingComment;
    private String tactic;
    private String tacticMapping;
    private String players;
    private String team;
    private Map<String, String> playersUsersMapping = new HashMap<>();
    private String partialMessage;
    private ActiveGame activeGame;

    public void setUp(String ownerName) {
        this.ownerName = ownerName;
        this.lastHearthBeatTime = new Date().getTime();
    }

    public Game() {

    }
}
