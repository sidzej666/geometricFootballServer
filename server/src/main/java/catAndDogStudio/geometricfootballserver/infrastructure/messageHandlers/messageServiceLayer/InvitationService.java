package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.MessageSender;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty.BaseMessageHandler;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvitationService extends BaseMessageHandler {

    private final ServerState serverState;

    public void cancelInvitation(Invitation invitation, Game ownerGame, boolean sendMessageToHost) {
        final Optional<Game> invitedPlayer = serverState.findWaitingPlayer(invitation.getInvitedPlayer());
        if (!invitedPlayer.isPresent()) {
            return;
        }
        if (invitedPlayer.get().getPlayerState() != PlayerState.AWAITS_GAME
                && invitedPlayer.get().getPlayerState() != PlayerState.AWAITING_INVITATION_DECISION) {
            if (sendMessageToHost) {
                sendMessage(ownerGame.getChannel(), ownerGame, rejectedByPlayer("invitedPlayer does not longer awaits game or invitation decision", invitation.getInvitedPlayer()));
            }
            return;
        }
        final Optional<Invitation> playerInvitation = findGuestInvitation(invitedPlayer.get().getInvitations(), ownerGame.getOwnerName());
        if (playerInvitation.isPresent()) {
            invitedPlayer.get().getInvitations().remove(playerInvitation.get());
        }
        ownerGame.getInvitations().remove(invitation);
        if (invitedPlayer.get().getPlayerState() == PlayerState.AWAITING_INVITATION_DECISION) {
            invitedPlayer.get().setPlayerState(PlayerState.AWAITS_GAME);
        }
        sendMessage(invitedPlayer.get().getChannel(), invitedPlayer.get(), rejectedByHost("rejected by host", ownerGame, invitation.getInvitedPlayer()));
        if (sendMessageToHost) {
            sendMessage(ownerGame.getChannel(), ownerGame, rejectedByHost("rejected by host", ownerGame, invitation.getInvitedPlayer()));
        }
    }

    public Optional<Invitation> findGuestInvitation(final List<Invitation> invitations,
                                                     final String gameHostName) {
        return invitations.stream()
                .filter(invitation -> invitation.getInvitator().equals(gameHostName))
                .findFirst();
    }

    public GeometricFootballResponse.Response rejectedByPlayer(final String message,
                                                                final String invitationId) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.INVITATION_RESULT)
                .setTeamInvitationResponse(GeometricFootballResponse.TeamInvitationResponse.newBuilder()
                        .setInvitationResult(GeometricFootballResponse.InvitationResult.REJECTED)
                        .setMessage(message)
                        .setId(invitationId)
                        .build())
                .build();
    }

    public GeometricFootballResponse.Response rejectedByHost(final String message,
                                                              final Game hostGame,
                                                              final String invitedPlayerName) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.INVITATION_RESULT)
                .setTeamInvitationResponse(GeometricFootballResponse.TeamInvitationResponse.newBuilder()
                        .setInvitationResult(GeometricFootballResponse.InvitationResult.REJECTED)
                        .setMessage(message)
                        .setId(hostGame.getOwnerName())
                        .setGameHostName(hostGame.getOwnerName())
                        .setInvitedPlayer(invitedPlayerName)
                        .build())
                .build();
    }
}
