package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.PlayersInTeamMessageCreator;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.TeamInfoMessageCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationAcceptedByGuestHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.AWAITS_GAME);
    private final PlayersInTeamMessageCreator playersInTeamMessageCreator;
    private final TeamInfoMessageCreator teamInfoMessageCreator;

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
        serverState.getWaitingForGamesOld().remove(channel);
        serverState.getPlayersInGame().put(channel, game);
        game.setPlayerState(PlayerState.GAME_GUEST);
        sendInvitationAcceptedToGameHostAndChangeHostState(hostChannel, hostGame, game, invitation);
        sendMessageToGuestAndUpdateGuestGameStateTransition(channel, game, invitation, hostGame);
    }

    private void sendMessageToGuestAndUpdateGuestGameStateTransition(SelectableChannel guestChannel, Game guestGame,
                                                                     Invitation hostInvitation, Game hostGame) {
        Invitation guestInvitation = guestGame.getInvitations().stream()
                .filter(i -> i.getInvitator().equals(hostInvitation.getInvitator()))
                .findFirst()
                .orElse(null);
        if (guestInvitation != null) {
            guestGame.getInvitations().remove(guestInvitation);
        }
        guestGame.setPlayerState(PlayerState.GAME_GUEST);
        sendMessage(guestChannel, guestGame, OutputMessages.INVITATION_ACCEPTED_BY_GUEST + ";" +
                hostInvitation.getInvitator() + ";" + guestGame.getOwnerName());
        sendMessage(guestChannel, guestGame, playersInTeamMessageCreator.message(hostGame, OutputMessages.TEAM_PLAYERS));
        teamInfoMessageCreator.sendAllAvailableTeamInfo(hostGame, guestChannel, guestGame);
    }

    private void sendInvitationAcceptedToGameHostAndChangeHostState(SelectableChannel hostChannel, Game hostGame,
                                                                    Game guestGame, Invitation hostInvitation) {
        hostGame.getInvitations().remove(hostInvitation);
        //hostGame.getPlayersInGame().put(hostInvitation.getInvitedPlayerChannel(), guestGame);
        //guestGame.setHostChannel(hostChannel);
        guestGame.setGrantedColor(hostInvitation.getPreferredColor());
        sendMessage(hostChannel, hostGame, OutputMessages.INVITATION_ACCEPTED_BY_GUEST + ";" +
                hostGame.getOwnerName() + ";" + guestGame.getOwnerName());
        sendMessage(hostChannel, hostGame, playersInTeamMessageCreator.message(hostGame, OutputMessages.TEAM_PLAYERS));
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}
