package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.PlayersInTeamMessageCreator;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class KickPlayerHandler extends BaseMessageHandler {

    private final ServerState serverState;
    private final PlayersInTeamMessageCreator playersInTeamMessageCreator;
    private final EnumSet<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final GeometricFootballRequest.KickPlayer kickPlayerReqeust = request.getKickPlayer();
        final Optional<Game> playerToKickOut = game.getPlayersInTeam().values().stream()
                .filter(g -> g.getOwnerName().equals(kickPlayerReqeust.getUsername()))
                .findFirst();
        if (!playerToKickOut.isPresent()) {
            sendMessage(channel, game, error("Player to kick out not found"));
            return;
        }
        final Game playerToKick = playerToKickOut.get();
        playerToKick.setPlayerState(PlayerState.AWAITS_GAME);
        game.getPlayersInTeam().remove(playerToKick.getChannel().id());
        serverState.moveFromPlayersInGamesToWaitingForGame(playerToKick.getChannel());
        GeometricFootballResponse.Response playerKickedOut = playerToKickOut(kickPlayerReqeust.getUsername());
        GeometricFootballResponse.Response teamPlayers = playersInTeamMessageCreator.createTeamInfo(game, false, true);
        sendPlayerKickedOutAndTeamInfo(game, playerKickedOut, teamPlayers);
        sendMessage(playerToKick.getChannel(), playerToKick, playerKickedOut);
        game.getPlayersInTeam().values().stream()
                .forEach(g -> sendPlayerKickedOutAndTeamInfo(g, playerKickedOut, teamPlayers));
    }

    private void sendPlayerKickedOutAndTeamInfo(final Game game,
                                                final GeometricFootballResponse.Response playerKickedOut,
                                                final GeometricFootballResponse.Response teamPlayers) {
        sendMessage(game.getChannel(), game, playerKickedOut);
        sendMessage(game.getChannel(), game, teamPlayers);
    }

    private GeometricFootballResponse.Response playerToKickOut(final String username) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.PLAYER_KICKED)
                .setPlayerKicked(GeometricFootballResponse.PlayerKickedResponse.newBuilder()
                        .setUsername(username)
                        .build())
                .build();
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}
