package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Set;

@Slf4j
public abstract class BaseMessageHandler extends MessageSender implements GeometricServerMessageHandler {
    @Override
    public final void handleMessage(SelectableChannel channel, Game game, String[] splittedMessage) {
        if (!validateState(game.getPlayerState())) {
            log.warn("not allowed game state for {} message, sender {}, senderState {}",
                    this.getClass().getSimpleName(), game.getOwnerName(), game.getPlayerState());
            return;
        }
        messageAction(channel, game, splittedMessage);
    }

    private boolean validateState(PlayerState playerState) {
        if (getPossibleStates() == null || getPossibleStates().contains(playerState)) {
            return true;
        }
        return false;
    }
    protected Set<PlayerState> getPossibleStates() {
        return null;
    }
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {

    }
}
