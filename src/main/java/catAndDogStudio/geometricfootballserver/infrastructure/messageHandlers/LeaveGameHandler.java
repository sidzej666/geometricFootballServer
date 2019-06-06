package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveGameHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST, PlayerState.GAME_GUEST,
            PlayerState.AWAITS_GAME, PlayerState.AWAITING_INVITATION_DECISION);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        if (game.getPlayerState() == PlayerState.GAME_HOST) {
            game.getInvitations().stream()
                    .forEach(i -> sendHostLeft(i.getInvitedPlayerChannel(), game));
            game.getInvitations().clear();
        } else if (game.getPlayerState() == PlayerState.AWAITS_GAME
                || game.getPlayerState() == PlayerState.AWAITING_INVITATION_DECISION) {
            Invitation invitation = game.getInvitations().stream()
                    .filter(i -> i.getInvitedPlayer().equals(game.getOwnerName()))
                    .findFirst()
                    .orElse(null);
            if (invitation != null) {
                game.getInvitations().stream()
                        .forEach(i -> sendPlayerLeftToHostAndRemoveInvitation(i));
                game.getInvitations().clear();
            }
        }
        game.setPlayerState(PlayerState.AUTHENTICATED);
        serverState.getHostedGames().remove(channel);
        serverState.getWaitingForGames().remove(channel);
        sendMessage(channel, game, OutputMessages.LEFT_FROM_GAME);
    }

    private void sendHostLeft(SelectableChannel invitedPlayerChannel, Game hostedGame) {
        sendMessage(invitedPlayerChannel, serverState.getWaitingForGames().get(invitedPlayerChannel),
                OutputMessages.HOST_LEFT + ";" + hostedGame.getOwnerName());
    }

    private void sendPlayerLeftToHostAndRemoveInvitation(Invitation i) {
        Game hostGame = serverState.getHostedGames().get(i.getInvitatorChannel());
        if (hostGame == null) {
            log.warn("invitation in host {} not removed, no such host in hostedGames", i.getInvitator());
            return;
        }
        Invitation invitationToRemoveInHostGame = hostGame.getInvitations().stream()
                .filter(inv -> inv.getInvitedPlayer().equals(i.getInvitedPlayer()))
                .findFirst()
                .orElse(null);
        if (invitationToRemoveInHostGame != null) {
            hostGame.getInvitations().remove(invitationToRemoveInHostGame);
        }
        sendMessage(i.getInvitatorChannel(), hostGame, OutputMessages.PLAYER_LEFT + ";" + i.getInvitedPlayer());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}
