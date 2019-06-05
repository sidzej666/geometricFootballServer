package catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlayersHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = Stream.of(PlayerState.GAME_HOST, PlayerState.AWAITS_GAME,
            PlayerState.IN_GAME)
            .collect(Collectors.toSet());
    private final static String WAITING = "WAITING";
    private final static String HOSTING = "HOSTING";

    @Override
    public void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        final String message;
        if (WAITING.equals(splittedMessage[1])) {
            message = preparePlayersMessage(game, serverState.getWaitingForGames().values());
        } else if (HOSTING.equals(splittedMessage[1])) {
            message = preparePlayersMessage(game, serverState.getHostedGames().values());
        } else {
            log.warn("invalid parameters for {}, sender {}, message {}",
                    GetPlayersHandler.class.getSimpleName(), game.getOwnerName(), splittedMessage);
            message = null;
        }
        if (message != null) {
            sendMessage(channel, game, message);
        }
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
    private String preparePlayersMessage(Game game, Collection<Game> games) {
        StringBuilder response = new StringBuilder(OutputMessages.PLAYERS);
        response.append(";");
        games.stream()
                .filter(g -> !g.getOwnerName().equals(game.getOwnerName()))
                .forEach(g -> {
                    response.append(g.getOwnerName());
                    response.append(";");
                    return;
                });
        return response.toString();
    }
}