package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public abstract class BaseMessageHandler extends MessageSender implements GeometricServerMessageHandler {
    @Override
    public final void handleMessage(final Channel channel, final Game game,
                                    final GeometricFootballRequest.Request request) {
        if (!validateState(game.getPlayerState())) {
            log.warn("not allowed game state for {} message, sender {}, senderState {}",
                    this.getClass().getSimpleName(), game.getOwnerName(), game.getPlayerState());
            notValidStateAction(channel, game, request);
            return;
        }
        messageAction(channel, game, request);
    }

    private boolean validateState(final PlayerState playerState) {
        if (getPossibleStates() == null || getPossibleStates().contains(playerState)) {
            return true;
        }
        return false;
    }
    protected Set<PlayerState> getPossibleStates() {
        return null;
    }
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {

    }
    protected void notValidStateAction(final Channel channel, final Game game,
                                       final GeometricFootballRequest.Request request) {
        sendMessage(channel, game, GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.ERROR)
                .setErrorResponse(GeometricFootballResponse.ErrorResponse.newBuilder()
                        .setMessage("Bad state for operation " + request.getType() + ", current state: " + game.getPlayerState())
                        .build())
                .build() );
    }

    protected GeometricFootballResponse.Response error(final String message) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.ERROR)
                .setErrorResponse(GeometricFootballResponse.ErrorResponse.newBuilder()
                        .setMessage(message)
                        .build())
                .build();
    }
}
