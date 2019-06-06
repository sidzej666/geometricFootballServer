package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationAcceptedByHostHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        String guestName = splittedMessage[1];
        Invitation invitation = game.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(guestName))
                .findAny()
                .orElse(null);
        if (invitation == null) {
            sendMessage(channel, game, OutputMessages.INVITATION_NOT_FOUND + ";" + guestName);
            return;
        }
        game.getInvitations().remove(invitation);
        Game guestGame = serverState.getWaitingForGames().get(invitation.getInvitedPlayerChannel());
        guestGame.setPlayerState(PlayerState.GAME_GUEST);
        game.getPlayersInGame().put(invitation.getInvitedPlayerChannel(), guestGame);
        sendPlayerJoinGameToAllPlayersInGame(game, guestName);
        game.getInvitations().remove(invitation);
        Invitation guestInvitation = guestGame.getInvitations().stream()
                .filter(i -> i.getInvitator().equals(game.getOwnerName()))
                .findFirst()
                .orElse(null);
        if (guestInvitation != null) {
            guestGame.getInvitations().remove(guestInvitation);
        }
        serverState.getWaitingForGames().remove(invitation.getInvitedPlayerChannel());
        sendMessage(channel, game, OutputMessages.KITTY_JOINED_GAME + ";" + invitation.getInvitedPlayer());
        sentPlayersListToAcceptedPlayer(game, invitation.getInvitedPlayerChannel(), guestGame);
    }

    private void sentPlayersListToAcceptedPlayer(Game game, SelectableChannel invitedPlayerChannel, Game guestGame) {
        String message = OutputMessages.TEAM_PLAYERS + ";" + game.getOwnerName() + ";";
        for(Game playerInGame : game.getPlayersInGame().values()) {
            message += playerInGame.getOwnerName() + ";";
        }
        sendMessage(invitedPlayerChannel, guestGame, message);
    }

    private void sendPlayerJoinGameToAllPlayersInGame(Game game, String newPlayer) {
        String message = OutputMessages.KITTY_JOINED_GAME + ";" + newPlayer;
        List<SelectableChannel> channels = new ArrayList<>();
        channels.addAll(game.getPlayersInGame().keySet());
        for (SelectableChannel channel: channels) {
            sendMessage(channel, game.getPlayersInGame().get(channel), message);
        }
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}
