package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.TeamInfoMessageCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Set;

//@Service
@RequiredArgsConstructor
@Slf4j
public class SetTacticMappingHandler extends BaseMessageHandler{
    private final ServerState serverState;
    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);
    private final TeamInfoMessageCreator teamInfoMessageCreator;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        final String tacticMapping = splittedMessage[1];
        //game.setTacticMapping(tacticMapping);
        game.getPlayersInGame().keySet().stream()
                .forEach(c -> sendTactic(c, game.getPlayersInGame().get(c), game));
    }

    private void sendTactic(SelectableChannel c, Game guestGame, Game hostGame) {
        sendMessage(c, guestGame, teamInfoMessageCreator.teamTacticMappingMessage(hostGame, OutputMessages.TEAM_TACTIC_MAPPING));
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
