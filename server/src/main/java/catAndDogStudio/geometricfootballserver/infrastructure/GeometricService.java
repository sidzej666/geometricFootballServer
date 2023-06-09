package catAndDogStudio.geometricfootballserver.infrastructure;

import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.InputMessages;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty.MessageHandlingStrategy;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class GeometricService implements ChannelWriter {
    private static final long DISCONNECTION_CHECK_INTERVAL = 5000l;
    private final Map<SelectableChannel, Game> awaitingAuthentication = new HashMap<>();
    private final Map<SelectableChannel, Game> hostedGames = new HashMap<>();
    private final Map<SelectableChannel, Game> waitingForGames = new HashMap<>();
    private final Map<SelectableChannel, Game> playersInGame = new HashMap<>();

    private final ServerState serverState;
    private final MessageHandlingStrategy messageHandlingStrategy;

    public void remove(SelectableChannel s) {
        hostedGames.remove(s);
        awaitingAuthentication.remove(s);
        waitingForGames.remove(s);
        Game game = playersInGame.get(s);
        if (game != null) {
            game.getPlayersInGame().remove(s);
            playersInGame.remove(s);
            sendPlayerLeftGameAndHandleHostDisconnection(game, s);
        }
    }

    public void handleMessage(final Channel channel, final Game game,
                              final GeometricFootballRequest.Request request) {
        messageHandlingStrategy.handleMessage(channel, game, request);
    }

    private void sendPlayerLeftGameAndHandleHostDisconnection(Game game, SelectableChannel s) {

    }

    public void awaitAuthentication(SelectableChannel channel, Game game) {
        awaitingAuthentication.put(channel, game);
    }

    public void checkIfSendHeathBeatMessageAndSendItIfIsNeeded(SelectableChannel channel, long currentTime,
                                                               Game game) throws IOException {
        if (currentTime - game.getLastHearthBeatTime() > DISCONNECTION_CHECK_INTERVAL) {
            final ByteBuffer buffer = ByteBuffer.wrap((InputMessages.HEARTHBEAT + "\n").getBytes());
            doWrite(buffer, (SocketChannel) channel);
            game.setLastHearthBeatTime(currentTime);
        }
    }

    public void storePartialMessage(SelectableChannel channel, Game game, String messageFromClient) {
        String partialMessage = game.getPartialMessage() == null ? "" : game.getPartialMessage();
        game.setPartialMessage(partialMessage + messageFromClient);
    }
}
