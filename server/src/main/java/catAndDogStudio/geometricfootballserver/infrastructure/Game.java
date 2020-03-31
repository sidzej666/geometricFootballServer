package catAndDogStudio.geometricfootballserver.infrastructure;

import com.cat_and_dog_studio.geometric_football.protocol.Model;
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
    private Model.Team team;
    private Model.TacticMapping tacticMapping;
    private Model.Tactic tactic;
    private Model.Players players;
    private Map<String, Model.PlayerOwner> playerFootballerMappings = new HashMap<>();
    private String partialMessage;
    private ActiveGame activeGame;

    public void setUp(String ownerName) {
        this.ownerName = ownerName;
        this.lastHearthBeatTime = new Date().getTime();
    }

    public Game() {

    }
}
