package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.InvitationsBusinessLogic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationRejectedByHostHandler extends BaseMessageHandler{
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST);
    private final InvitationsBusinessLogic invitationsBusinessLogic;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        String guestName = splittedMessage[1];
        Invitation invitation = game.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(guestName))
                .findAny()
                .orElse(null);
        if (invitation == null || serverState.getWaitingForGamesOld().get(invitation.getInvitedPlayerChannel()) == null) {
            sendMessage(channel, game, OutputMessages.INVITATION_NOT_FOUND + ";" + guestName);
            return;
        }
        Game guestGame = serverState.getWaitingForGamesOld().get(invitation.getInvitedPlayerChannel());
        game.getInvitations().remove(invitation);
        sendMessage(channel, game, OutputMessages.KITTY_INVITATION_REJECTED + ";" + guestName);
        //invitationsBusinessLogic.sendPlayerInvitationRejectedAndTransitionGuestState(game.getOwnerName(), guestGame, invitation.getInvitedPlayerChannel());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}
