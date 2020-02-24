package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.LeaveGameService;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwaitGameHandler extends BaseMessageHandler {

    private final ChannelGroup waitingForGames;
    private final ServerState serverState;

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return PlayerState.AWAITS_GAME.possibleStatesForTransition;
    }

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final GeometricFootballRequest.AwaitGame awaitGame = request.getAwaitGame();
        game.setPlayerState(PlayerState.AWAITS_GAME);
        game.setPreferredColor(awaitGame.getPreferredColor());
        game.setWaitingComment(awaitGame.getWaitingMessage());
        waitingForGames.add(channel);
        sendMessage(channel, game, awaitGame(awaitGame));
    }

    private GeometricFootballResponse.Response awaitGame(final GeometricFootballRequest.AwaitGame awaitGame) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.AWAITING_FOR_GAME)
                .setAwaitGameResponse(GeometricFootballResponse.AwaitGameResponse.newBuilder()
                        .setUsername(awaitGame.getUsername())
                        .setWaitingMessage(awaitGame.getWaitingMessage())
                        .setPreferredColor(awaitGame.getPreferredColor())
                        .build())
                .build();
    }
}
