package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty.BaseMessageHandler;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveGameService extends BaseMessageHandler {
    private final ServerState serverState;
    private final ChannelGroup waitingForGames;
    private final ChannelGroup hosts;
    private final ChannelGroup playersInGames;
    @Getter
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST, PlayerState.GAME_GUEST,
            PlayerState.AWAITS_GAME, PlayerState.AWAITING_INVITATION_DECISION);

    public void leaveGame(Channel channel, Game game, boolean isDisconnection) {
        if (game.getPlayerState() == PlayerState.GAME_HOST) {
            game.getInvitations().stream()
                    .forEach(i -> {
                        //sendHostLeft(i.getInvitedPlayerChannel(), game, serverState.getWaitingForGamesOld());
                        Game guestGame = serverState.getWaitingForGamesOld().get(i);
                        Invitation invitation = guestGame.getInvitations().stream()
                                .filter(inv -> inv.getInvitator().equals(game.getOwnerName()))
                                .findAny()
                                .orElse(null);
                        if (invitation != null) {
                            guestGame.getInvitations().remove(invitation);
                        }
                    });
            game.getInvitations().clear();
            game.getPlayersInGame().keySet().stream()
                    .forEach(g -> {
                        sendHostLeft(g, game, serverState.getPlayersInGame());
                        Game guestGame = serverState.getPlayersInGame().get(g);
                        guestGame.setPlayerState(PlayerState.AWAITS_GAME);
                        serverState.getPlayersInGame().remove(g);
                        serverState.getWaitingForGamesOld().put(g, guestGame);
                    });
            game.getPlayersInGame().clear();
        } else if (game.getPlayerState() == PlayerState.AWAITS_GAME
                || game.getPlayerState() == PlayerState.AWAITING_INVITATION_DECISION) {
            game.getInvitations()
                    .forEach(i -> sendPlayerLeftToHostAndRemoveInvitation(i));
            game.getInvitations().clear();
            waitingForGames.remove(channel.id());
        } else if (game.getPlayerState() == PlayerState.GAME_GUEST) {
            sendPlayerLeftToHostAndAllOtherPlayerInGameAndRemovePlayerFromGame(channel, game);
            playersInGames.remove(channel.id());
        }
        if (!isDisconnection) {
            game.setPlayerState(PlayerState.AUTHENTICATED);
        }
        if (isDisconnection) {
            serverState.getGames().remove(channel.id());
        }
        //sendMessage(channel, game, OutputMessages.LEFT_FROM_GAME);
    }

    private void sendHostLeft(SelectableChannel invitedPlayerChannel, Game hostedGame,
                              Map<SelectableChannel, Game> channels) {
        /*
        sendMessage(invitedPlayerChannel, channels.get(invitedPlayerChannel),
                OutputMessages.HOST_LEFT + ";" + hostedGame.getOwnerName());

         */
    }

    private void sendPlayerLeftToHostAndRemoveInvitation(Invitation invitation) {
        Game hostGame = serverState.getGames().get(invitation.getInvitatorChannel().id());
        if (hostGame == null) {
            log.warn("invitation in host {} not removed, no such host in hostedGames", invitation.getInvitator());
            return;
        }
        Invitation invitationToRemoveInHostGame = hostGame.getInvitations().stream()
                .filter(inv -> inv.getInvitedPlayer().equals(invitation.getInvitedPlayer()))
                .findFirst()
                .orElse(null);
        if (invitationToRemoveInHostGame != null) {
            hostGame.getInvitations().remove(invitationToRemoveInHostGame);
        }
        sendMessage(hostGame.getChannel(), hostGame, invitationRejected(invitation));
    }
    private void sendPlayerLeftToHostAndAllOtherPlayerInGameAndRemovePlayerFromGame(Channel guestChannel, Game guestGame) {
        Game hostGame = serverState.getGames().get(guestGame.getHostChannel().id());
        hostGame.getPlayersInTeam().remove(guestChannel.id());
        final GeometricFootballResponse.Response playerLeft = playerLeft(guestGame.getOwnerName());
        sendMessage(hostGame.getChannel(), hostGame, playerLeft);
        for (Game teamMemberGame : hostGame.getPlayersInTeam().values()) {
            sendMessage(teamMemberGame.getChannel(), teamMemberGame, playerLeft);
        }
    }

    private GeometricFootballResponse.Response playerLeft(final String guestName) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.LEAVE_TEAM)
                .setLeaveTeam(GeometricFootballResponse.LeaveTeamResponse.newBuilder()
                        .setUsername(guestName)
                        .setHostLeft(false)
                        .build())
                .build();
    }

    private GeometricFootballResponse.Response invitationRejected(final Invitation invitation) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.INVITATION_RESULT)
                .setTeamInvitationResponse(GeometricFootballResponse.TeamInvitationResponse.newBuilder()
                        .setInvitationResult(GeometricFootballResponse.InvitationResult.INVITATION_REJECTED_BY_GUEST)
                        .setMessage("invitation created")
                        .setId(invitation.getInvitedPlayer())
                        .setGameHostName(invitation.getInvitator())
                        .setInvitedPlayer(invitation.getInvitedPlayer())
                        .setGrantedColor(invitation.getPreferredColor())
                        .build())
                .build();
    }
}
