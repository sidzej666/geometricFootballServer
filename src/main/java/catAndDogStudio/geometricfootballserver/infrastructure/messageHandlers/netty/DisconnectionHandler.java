package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.LeaveGameService;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DisconnectionHandler extends BaseMessageHandler {

    private final LeaveGameService leaveGameService;

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final GeometricFootballRequest.Disconnect disconnect = request.getDisconnect();
        if (leaveGameService.getAllowedStates().contains(game.getPlayerState())) {
           leaveGameService.leaveGame(channel, game, true);
        }
        sendMessage(channel, game, disconnect());
        channel.disconnect();
    }

    private GeometricFootballResponse.Response disconnect() {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.DISCONNECT)
                .setDisconnectResponse(GeometricFootballResponse.DisconnectResponse.newBuilder()
                        .setMessage("Bye")
                        .build())
                .build();
    }
}
