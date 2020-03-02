package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.MessageSender;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;

@Service
public class InvitationsBusinessLogic extends MessageSender {
    public void sendPlayerInvitationRejectedAndTransitionGuestState(String ownerName, Game guestGame, SelectableChannel invitedPlayerChannel) {
        guestGame.setPlayerState(PlayerState.AWAITS_GAME);
        Invitation guestInvitation = guestGame.getInvitations().stream()
                .filter(i -> i.getInvitator().equals(ownerName))
                .findFirst()
                .orElse(null);
        guestGame.getInvitations().remove(guestInvitation);
        sendMessage(invitedPlayerChannel, guestGame, OutputMessages.KITTY_NOT_WANTED + ";" + ownerName + ";"
                + guestGame.getOwnerName());
    }
}
