package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.*;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.ReadyForGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

//@Service
@RequiredArgsConstructor
@Slf4j
public class GoBackToTeamCreationHandler extends BaseMessageHandler {
    private final static Set<PlayerState> availableStates = EnumSet.of(PlayerState.WAIT_FOR_OPPONENTS);
    private final ServerState serverState;
    private final ReadyForGameService readyForGameService;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        game.setPlayerState(PlayerState.GAME_HOST);
        sendGoBackToTeamCreationMessageMessage(channel, game);
        game.getPlayersInGame().keySet().stream()
                .forEach(k -> sendGoBackToTeamCreationMessageMessage(k, game.getPlayersInGame().get(k)));
        //readyForGameService.goBackToTeamCreationServerTransition(channel, game);
    }

    private void sendGoBackToTeamCreationMessageMessage(SelectableChannel channel, Game game) {
        sendMessage(channel, game, OutputMessages.getGoBackToTeamPreparationTransition());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
