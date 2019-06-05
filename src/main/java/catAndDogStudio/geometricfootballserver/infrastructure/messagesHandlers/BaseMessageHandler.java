package catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.Set;

@Slf4j
public abstract class BaseMessageHandler implements GeometricServerMessageHandler {
    protected void sendMessage(SelectableChannel channel, Game game, String message) {
        try {
            final ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
            doWrite(buffer, (SocketChannel) channel);
            log.debug("message sent to {}, message: {}", game.getOwnerName(), message);
        } catch (Exception e){
            log.error("Error while sending message in {}, receiver {}, message {}" , this.getClass().getSimpleName(),
                    game.getOwnerName(), message);
        }
    }
    void doWrite(final ByteBuffer buffer, final SocketChannel channel) throws IOException {
        if (Objects.isNull(buffer) || Objects.isNull(channel)) {
            throw new IllegalArgumentException("Required buffer and channel.");
        }

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
    @Override
    public void handleMessage(SelectableChannel channel, Game game, String[] splittedMessage) {
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
