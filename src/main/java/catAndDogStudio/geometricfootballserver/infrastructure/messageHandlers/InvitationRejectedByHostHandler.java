package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
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
public class InvitationRejectedByHostHandler extends BaseMessageHandler{
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        String guestName = splittedMessage[1];
        Invitation invitation = game.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(guestName))
                .findAny()
                .orElse(null);
        if (invitation == null || serverState.getWaitingForGames().get(invitation.getInvitedPlayerChannel()) == null) {
            sendMessage(channel, game, OutputMessages.INVITATION_NOT_FOUND + ";" + guestName);
            return;
        }
        Game guestGame = serverState.getWaitingForGames().get(invitation.getInvitedPlayerChannel());
        game.getInvitations().remove(invitation);
        sendMessage(channel, game, OutputMessages.KITTY_INVITATION_REJECTED + ";" + guestName);
        sendPlayerInvitationRejectedAndTransitionGuestState(game.getOwnerName(), guestGame, invitation.getInvitedPlayerChannel());
    }

    private void sendPlayerInvitationRejectedAndTransitionGuestState(String ownerName, Game guestGame, SelectableChannel invitedPlayerChannel) {
        guestGame.setPlayerState(PlayerState.AWAITS_GAME);
        Invitation guestInvitation = guestGame.getInvitations().stream()
                .filter(i -> i.getInvitator().equals(ownerName))
                .findFirst()
                .orElse(null);
        guestGame.getInvitations().remove(guestInvitation);
        sendMessage(invitedPlayerChannel, guestGame, OutputMessages.KITTY_NOT_WANTED + ";" + ownerName + ";"
            + guestGame.getOwnerName());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}
