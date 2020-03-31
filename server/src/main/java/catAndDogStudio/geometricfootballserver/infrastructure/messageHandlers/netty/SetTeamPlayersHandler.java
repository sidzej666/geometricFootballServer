package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import com.cat_and_dog_studio.geometric_football.protocol.Model;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetTeamPlayersHandler extends BaseMessageHandler {

    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final Model.Players players = request.getPlayers();
        game.setPlayers(players);
        sendMessage(channel, game, ok());
        game.getPlayersInTeam().values().stream()
                .forEach(g -> sendTeamPlayers(g, players));
    }

    private void sendTeamPlayers(final Game game, final Model.Players players) {
        sendMessage(game.getChannel(), game, teamPlayersResponse(players));
    }

    private GeometricFootballResponse.Response teamPlayersResponse(final Model.Players players) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.SET_TEAM_PLAYERS)
                .setPlayers(players)
                .build();
    }

    private GeometricFootballResponse.Response ok() {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.OK)
                .setOkResponse(GeometricFootballResponse.OkResponse.newBuilder()
                        .setMessage("team players set")
                        .build())
                .build();
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
