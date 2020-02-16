package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageHandlingStrategy {

    private final AuthenticationHandler authenticationHandler;
    private final DisconnectionHandler disconnectionHandler;
    private final HostGameHandler hostGameHandler;
    private final AwaitGameHandler awaitGameHandler;
    private final GetPlayersHandler getPlayersHandler;
    private Map<GeometricFootballRequest.RequestType, GeometricServerMessageHandler> handlers = new HashMap<>();

    @PostConstruct
    public void setHandlers() {
        handlers.put(GeometricFootballRequest.RequestType.AUTHENTICATION, authenticationHandler);
        handlers.put(GeometricFootballRequest.RequestType.DISCONNECT, disconnectionHandler);
        handlers.put(GeometricFootballRequest.RequestType.HOST_GAME, hostGameHandler);
        handlers.put(GeometricFootballRequest.RequestType.AWAIT_GAME, awaitGameHandler);
        handlers.put(GeometricFootballRequest.RequestType.GET_PLAYERS, getPlayersHandler);
    }

    public void handleMessage(final Channel channel, final Game game,
                              final GeometricFootballRequest.Request request) {
        if (request.getType() == GeometricFootballRequest.RequestType.PING) {
            return;
        }
        log.debug("message received from " + game.getOwnerName() + " " + request);

        GeometricServerMessageHandler handler = handlers.get(request.getType());
        if (handler != null) {
            handler.handleMessage(channel, game, request);
            return;
        }

        log.warn("no valid strategy found for message, sender: {}, message: {}", game.getOwnerName(), request);
    }
}
