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
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetTeamPlayersUsersMappingHandler extends BaseMessageHandler {

    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final Model.PlayerFootballerMappings playerFootballerMappings = request.getPlayerFootballerMappings();
        for (Model.PlayerOwner playerOwner : playerFootballerMappings.getPlayerOwnersList()) {
            game.getPlayerFootballerMappings().put(playerOwner.getPlayerId(), playerOwner);
        }
        sendMessage(channel, game, ok());
        game.getPlayersInTeam().values().stream()
                .forEach(g -> sendPlayerFootballerMappings(g, game.getPlayerFootballerMappings()));
    }

    private void sendPlayerFootballerMappings(final Game game, final Map<String, Model.PlayerOwner> playerFootballerMappings) {
        sendMessage(game.getChannel(), game, sendPlayerFootballeResponse(playerFootballerMappings));
    }

    private GeometricFootballResponse.Response sendPlayerFootballeResponse(final Map<String, Model.PlayerOwner> playerFootballerMappings) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.SET_TEAM_PLAYERS_USERS_MAPPING)
                .setPlayerFootballerMappings(Model.PlayerFootballerMappings.newBuilder()
                        .addAllPlayerOwners(playerFootballerMappings.values())
                        .build())
                .build();
    }

    private GeometricFootballResponse.Response ok() {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.OK)
                .setOkResponse(GeometricFootballResponse.OkResponse.newBuilder()
                        .setMessage("team players to users mapping set")
                        .build())
                .build();
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
