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
public class CancelInvitationHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.AWAITING_INVITATION_DECISION,
            PlayerState.GAME_HOST);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        final String invitedPlayer = splittedMessage[1];
        SelectableChannel hostChannel = serverState.getHostPlayer(invitedPlayer);
        SelectableChannel guestChannel = serverState.getWaitingPlayer(invitedPlayer);
        if (hostChannel != null && game.getPlayerState() == PlayerState.AWAITING_INVITATION_DECISION) {
            Invitation invitation = game.getInvitations().stream()
                    .filter(i -> i.getInvitator().equals(invitedPlayer))
                    .findAny()
                    .orElse(null);
            if (invitation == null) {
                sendMessage(channel, game, OutputMessages.INVITATION_NOT_FOUND + ";" + invitedPlayer);
            }
            cancelInvitationByGuest(invitation, game);
            game.setPlayerState(PlayerState.AWAITS_GAME);
            return;
        } else if (guestChannel != null && game.getPlayerState() == PlayerState.GAME_HOST) {
            Invitation invitation = game.getInvitations().stream()
                    .filter(i -> i.getInvitedPlayer().equals(invitedPlayer))
                    .findAny()
                    .orElse(null);
            if (invitation == null) {
                sendMessage(channel, game, OutputMessages.INVITATION_NOT_FOUND + ";" + invitedPlayer);
            }
            cancelInvitationByHost(invitation, game);
            return;
        } else {
            sendMessage(channel, game, OutputMessages.INVITATION_NOT_FOUND + ";" + invitedPlayer);
            return;
        }
    }

    private void cancelInvitationByHost(Invitation invitation, Game hostGame) {
        Game guestGame = serverState.getWaitingForGames().get(invitation.getInvitedPlayerChannel());
        sendMessage(invitation.getInvitedPlayerChannel(), guestGame,
                OutputMessages.INVITATION_CANCELLED + ";" + invitation.getInvitator());
        Invitation guestInvitation = guestGame.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(invitation.getInvitedPlayer()))
                .findFirst()
                .orElse(null);
        if (guestInvitation != null) {
            guestGame.getInvitations().remove(guestInvitation);
        }
        hostGame.getInvitations().remove(invitation);
        sendMessage(invitation.getInvitatorChannel(), hostGame,
                OutputMessages.INVITATION_CANCELLED + ";" + invitation.getInvitedPlayer());
    }

    private void cancelInvitationByGuest(Invitation invitation, Game guestGame) {
        Game hostGame = serverState.getHostedGames().get(invitation.getInvitatorChannel());
        sendMessage(invitation.getInvitatorChannel(), hostGame,
                OutputMessages.INVITATION_CANCELLED + ";" + invitation.getInvitedPlayer());
        Invitation hostInvitation = hostGame.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(invitation.getInvitedPlayer()))
                .findFirst()
                .orElse(null);
        if (hostInvitation != null) {
            hostGame.getInvitations().remove(hostInvitation);
        }
        sendMessage(invitation.getInvitedPlayerChannel(), guestGame,
                OutputMessages.INVITATION_CANCELLED + ";" + hostGame.getOwnerName());
        guestGame.getInvitations().remove(invitation);
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
