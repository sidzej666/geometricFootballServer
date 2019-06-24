package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetTacticMappingHandler extends BaseMessageHandler{
    private final ServerState serverState;
    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        final String tacticMapping = splittedMessage[1];
        game.setTactic(tacticMapping);
        game.getPlayersInGame().keySet().stream()
                .forEach(c -> sendTactic(c, game.getPlayersInGame().get(c), tacticMapping));
    }

    private void sendTactic(SelectableChannel c, Game guestGame, String tacticMapping) {
        sendMessage(c, guestGame, OutputMessages.TEAM_TACTIC_MAPPING + Constants.MESSAGE_SEPARATOR + tacticMapping);
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
