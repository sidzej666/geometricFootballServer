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
public class InvitationRejectedByGuestHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.AWAITS_GAME);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        final String hostName = splittedMessage[1];
        SelectableChannel hostChannel = serverState.getHostPlayer(hostName);
        if (hostChannel == null) {
            sendMessage(channel, game, OutputMessages.NO_SUCH_HOSTING_KITTY + ";" + hostName);
            return;
        }
        Game hostGame = serverState.getHostedGames().get(hostChannel);
        Invitation invitation = hostGame.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(game.getOwnerName()))
                .findAny()
                .orElse(null);
        if (invitation == null) {
            sendMessage(channel, game, OutputMessages.INVITATION_NOT_FOUND + ";" + hostGame.getOwnerName());
            return;
        }
        hostMessageAndTransition(hostChannel, hostGame, invitation, game);
        guestMessageAndTransition(channel, hostGame, game);
    }

    private void guestMessageAndTransition(SelectableChannel guestChannel, Game hostGame, Game guestGame) {
        Invitation guestInvitation = guestGame.getInvitations().stream()
                .filter(i -> i.getInvitator().equals(hostGame.getOwnerName()))
                .findFirst()
                .orElse(null);
        if (guestInvitation != null) {
            guestGame.getInvitations().remove(guestInvitation);
        }
        sendMessage(guestChannel, guestGame, OutputMessages.INVITATION_REJECTED_BY_GUEST + ";" +
                hostGame.getOwnerName() + ";" + guestGame.getOwnerName());
    }

    private void hostMessageAndTransition(SelectableChannel hostChannel, Game hostGame, Invitation hostInvitation,
                                          Game guestGame) {
        hostGame.getInvitations().remove(hostInvitation);
        sendMessage(hostChannel, hostGame, OutputMessages.INVITATION_REJECTED_BY_GUEST + ";"
                + hostGame.getOwnerName() + ";" + guestGame.getOwnerName());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return super.getPossibleStates();
    }
}
