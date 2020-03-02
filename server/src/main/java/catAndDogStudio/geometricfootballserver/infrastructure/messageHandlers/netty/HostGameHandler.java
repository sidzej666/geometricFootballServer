package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class HostGameHandler extends BaseMessageHandler {

    private final ServerState serverState;

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return PlayerState.GAME_HOST.possibleStatesForTransition;
    }

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final GeometricFootballRequest.HostGame hostGame = request.getHostGame();
        game.setPlayerState(PlayerState.GAME_HOST);
        game.setGrantedColor(hostGame.getHostColor());
        game.setGameName(hostGame.getGameName());
        serverState.moveToHosts(channel);
        sendMessage(channel, game, gameHosted(hostGame));
    }

    private GeometricFootballResponse.Response gameHosted(final GeometricFootballRequest.HostGame hostGame) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.GAME_HOSTED)
                .setHostGameResponse(GeometricFootballResponse.HostGameResponse.newBuilder()
                        .setUsername(hostGame.getUsername())
                        .setGameName(hostGame.getGameName())
                        .setHostColor(hostGame.getHostColor())
                        .build())
                .build();
    }
}
