package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class HostGameHandler extends BaseMessageHandler {
    private final ServerState serverState;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        if (serverState.getHostedGames().get(channel) != null) {
            sendMessage(channel, game, OutputMessages.CUTE_KITTY_YOU_ARE_ALREADY_HOSTING_A_GAME);
            return;
        }
        game.setPlayerState(PlayerState.GAME_HOST);
        game.setGrantedColor(splittedMessage[2]);
        serverState.getHostedGames().put(channel, game);
        sendMessage(channel, game, OutputMessages.GAME_HOSTED + ";" + game.getOwnerName());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return PlayerState.GAME_HOST.possibleStatesForTransition;
    }
}
