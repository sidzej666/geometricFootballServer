package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

@Slf4j
public class MessageSender {
    protected final void sendMessage(SelectableChannel channel, Game game, String message) {
        try {
            final ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
            doWrite(buffer, (SocketChannel) channel);
            log.debug("{} message sent to {}, message: {}", this.getClass().getSimpleName(), game.getOwnerName(), message);
        } catch (Exception e){
            log.error("Error while sending message in {}, receiver {}, message {}" , this.getClass().getName(),
                    game.getOwnerName(), message);
            log.error("Details: ", e);
        }
    }
    private void doWrite(final ByteBuffer buffer, final SocketChannel channel) throws IOException {
        if (Objects.isNull(buffer) || Objects.isNull(channel)) {
            throw new IllegalArgumentException("Required buffer and channel.");
        }

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
}
