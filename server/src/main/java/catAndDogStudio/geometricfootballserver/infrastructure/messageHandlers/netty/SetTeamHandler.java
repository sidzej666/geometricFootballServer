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
public class SetTeamHandler extends BaseMessageHandler {

    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final Model.Team team = request.getTeam();
        game.setTeam(team);
        sendMessage(channel, game, ok());
        game.getPlayersInTeam().values().stream()
                .forEach(g -> sendTeam(g, team));
    }

    private void sendTeam(final Game game, final Model.Team team) {
        sendMessage(game.getChannel(), game, teamResponse(team));
    }

    private GeometricFootballResponse.Response teamResponse(final Model.Team team) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.SET_TEAM)
                .setTeam(team)
                .build();
    }

    private GeometricFootballResponse.Response ok() {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.OK)
                .setOkResponse(GeometricFootballResponse.OkResponse.newBuilder()
                        .setMessage("team set")
                        .build())
                .build();
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
