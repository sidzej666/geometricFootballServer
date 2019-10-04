package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.*;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.InvitationsBusinessLogic;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.ReadyForGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadyForGameHandler  extends BaseMessageHandler{
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST);
    private final InvitationsBusinessLogic invitationsBusinessLogic;
    private final ReadyForGameService readyForGameService;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        final String validationMessage = validateGame(game);
        if (validationMessage != null) {
            sendMessage(channel, game, OutputMessages.READY_FOR_GAME_VALIDATION_ERROR
                    + Constants.MESSAGE_SEPARATOR + validationMessage);
            return;
        }
        game.getInvitations().forEach(i -> cancelInvitation(i, game));
        game.getInvitations().clear();
        waitForOpponents(channel, game);
        readyForGameService.transitionServerStateAndFindOpponent(channel, game);
    }

    private void waitForOpponents(SelectableChannel channel, Game game) {
        game.setPlayerState(PlayerState.WAIT_FOR_OPPONENTS);
        game.setReadyForGameTime((new Date()).getTime());
        sendReadyForGameTransitionMessage(channel, game);
        game.getPlayersInGame().keySet()
                .forEach(guestChannel -> sendReadyForGameTransitionMessage(guestChannel, game.getPlayersInGame().get(guestChannel)));
    }

    private void cancelInvitation(Invitation i, Game ownerGame) {
        SelectableChannel invitedPlayerChannel = serverState.getWaitingPlayer(i.getInvitedPlayer());
        Game invitedPlayerGame = serverState.getWaitingForGames().get(invitedPlayerChannel);
        invitationsBusinessLogic.sendPlayerInvitationRejectedAndTransitionGuestState(ownerGame.getOwnerName(),
                invitedPlayerGame, invitedPlayerChannel);
    }

    private void sendReadyForGameTransitionMessage(SelectableChannel channel, Game game) {
        sendMessage(channel, game, OutputMessages.getReadyForGameTransitionMessage());
    }

    private String validateGame(Game game) {
        if (game.getTeam() == null) {
            return "team not set";
        }
        if (game.getPlayers() == null) {
            return "players not set";
        }
        if (game.getPlayersUsersMapping() == null || game.getPlayersUsersMapping().isEmpty()) {
            return "players-users mapping not set";
        }
        if (game.getTactic() == null) {
            return "tactic not set";
        }
        if (game.getTacticMapping() == null) {
            return "tactic mapping not set";
        }
        return null;
        //TODO tactic mapping validation? are all players set?
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}
