package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class MessageSender {
    protected final void sendMessage(final Channel channel, final Game game,
                                     final GeometricFootballResponse.Response response) {
        try {
            doWrite(channel, response);
            log.debug("{} message sent to {}, message: {}", this.getClass().getSimpleName(), game.getOwnerName(), response);
        } catch (Exception e){
            log.error("Error while sending message in {}, receiver {}, message {}" , this.getClass().getName(),
                    game.getOwnerName(), response);
            log.error("Details: ", e);
        }
    }
    private void doWrite(final Channel channel, final GeometricFootballResponse.Response response)
            throws IOException {
        if (Objects.isNull(response) || Objects.isNull(response)) {
            throw new IllegalArgumentException("Required response and channel.");
        }
        channel.writeAndFlush(response);
    }
}
