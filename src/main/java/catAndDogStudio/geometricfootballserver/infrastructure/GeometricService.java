package catAndDogStudio.geometricfootballserver.infrastructure;

import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.InputMessages;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.MessageHandlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

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

    public void handleMessage(SelectableChannel channel, Game game, String message) {
        messageHandlingStrategy.handleMessage(channel, game, message);
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

}
