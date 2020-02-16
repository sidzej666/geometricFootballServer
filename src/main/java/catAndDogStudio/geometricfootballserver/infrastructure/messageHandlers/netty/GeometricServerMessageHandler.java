package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import io.netty.channel.Channel;

public interface GeometricServerMessageHandler {
    void handleMessage(final Channel channel, final Game game, final GeometricFootballRequest.Request request);
}
