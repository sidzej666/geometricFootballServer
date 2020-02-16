package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.MessageSender;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
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
public class LeaveGameService extends MessageSender {
    private final ServerState serverState;
    private final ChannelGroup hosts;
    @Getter
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST, PlayerState.GAME_GUEST,
            PlayerState.AWAITS_GAME, PlayerState.AWAITING_INVITATION_DECISION);

    public void leaveGame(Channel channel, Game game, boolean isDisconnection) {
        if (game.getPlayerState() == PlayerState.GAME_HOST) {
            game.getInvitations().stream()
                    .forEach(i -> {
                        sendHostLeft(i.getInvitedPlayerChannel(), game, serverState.getWaitingForGames());
                        Game guestGame = serverState.getWaitingForGames().get(i);
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
                        serverState.getWaitingForGames().put(g, guestGame);
                    });
            game.getPlayersInGame().clear();
        } else if (game.getPlayerState() == PlayerState.AWAITS_GAME
                || game.getPlayerState() == PlayerState.AWAITING_INVITATION_DECISION) {
            game.getInvitations()
                    .forEach(i -> sendPlayerLeftToHostAndRemoveInvitation(i));
            game.getInvitations().clear();
        } else if (game.getPlayerState() == PlayerState.GAME_GUEST) {
            //sendPlayerLeftToHostAndAllOtherPlayerInGameAndRemovePlayerFromGame(channel, game);
        }
        if (!isDisconnection) {
            game.setPlayerState(PlayerState.AUTHENTICATED);
        }
        serverState.getHostedGames().remove(channel);
        serverState.getWaitingForGames().remove(channel);
        serverState.getTeamsWaitingForOpponents().remove(channel);
        serverState.getPlayersInGame().remove(channel);
        //sendMessage(channel, game, OutputMessages.LEFT_FROM_GAME);
    }

    private void sendHostLeft(SelectableChannel invitedPlayerChannel, Game hostedGame,
                              Map<SelectableChannel, Game> channels) {
        sendMessage(invitedPlayerChannel, channels.get(invitedPlayerChannel),
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
    private void sendPlayerLeftToHostAndAllOtherPlayerInGameAndRemovePlayerFromGame(SelectableChannel guestChannel, Game guestGame) {
        Game hostGame = serverState.getHostedGames().get(guestGame.getHostChannel());
        if (hostGame == null) {
            hostGame = serverState.getTeamsWaitingForOpponents().get(guestGame.getHostChannel());
        }
        if (hostGame == null) {
            hostGame = serverState.getPlayersInGame().get(guestGame.getHostChannel());
        }
        if (hostGame == null) {
            return;
        }
        SelectableChannel hostChannel = guestGame.getHostChannel();
        hostGame.getPlayersInGame().remove(guestChannel);
        sendMessage(hostChannel, hostGame, OutputMessages.PLAYER_LEFT + ";" + guestGame.getOwnerName());
        for (SelectableChannel playerChannel : hostGame.getPlayersInGame().keySet()) {
            Game playerGame = hostGame.getPlayersInGame().get(playerChannel);
            sendMessage(playerChannel, playerGame, OutputMessages.PLAYER_LEFT + ";" + guestGame.getOwnerName());
        }
    }
}
