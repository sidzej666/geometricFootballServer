package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.PlayersInTeamMessageCreator;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamInvitationHandler extends BaseMessageHandler {

    private final ServerState serverState;
    private final PlayersInTeamMessageCreator playersInTeamMessageCreator;

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return EnumSet.of(PlayerState.AWAITS_GAME, PlayerState.GAME_GUEST, PlayerState.AWAITING_INVITATION_DECISION,
                PlayerState.GAME_HOST);
    }

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final GeometricFootballRequest.TeamInvitation teamInvitation = request.getTeamInvitation();
        switch (teamInvitation.getTeamInvitationDirection()) {
            case FROM_HOST_TO_PLAYER:
                if (game.getPlayerState() != PlayerState.GAME_HOST) {
                    notValidStateAction(channel, game, request);
                    return;
                }
                final Optional<Invitation> invitation = findHostInvitation(game.getInvitations(), teamInvitation);
                final Optional<Game> invitedPlayer = serverState.findWaitingPlayer(teamInvitation.getInvitedPlayer());
                if (!invitedPlayer.isPresent()) {
                    sendMessage(channel, game, rejectedByPlayer("invitedPlayer does not exists", teamInvitation));
                    return;
                }
                final Optional<Invitation> playerInvitation = findGuestInvitation(invitedPlayer.get().getInvitations(), teamInvitation);
                switch (teamInvitation.getTeamInvitationAction()) {
                    case CREATE:
                        if (invitation.isPresent()) {
                            sendMessage(channel, game, error("invitation already exists"));
                            return;
                        }
                        if (invitedPlayer.get().getPlayerState() != PlayerState.AWAITS_GAME) {
                            sendMessage(channel, game, rejectedByPlayer("invitedPlayer does not longer awaits game", teamInvitation));
                            return;
                        }
                        final Invitation newInvitation = Invitation.builder()
                                .creationTime(new Date().getTime())
                                .invitator(game.getOwnerName())
                                .invitatorChannel(game.getChannel())
                                .invitedPlayer(invitedPlayer.get().getOwnerName())
                                .invitedPlayerChannel(invitedPlayer.get().getChannel())
                                .preferredColor(teamInvitation.getPreferredColor())
                                .build();
                        game.getInvitations().add(newInvitation);
                        invitedPlayer.get().getInvitations().add(newInvitation);
                        sendMessage(channel, game, invitationCreated(game, teamInvitation));
                        sendMessage(invitedPlayer.get().getChannel(), invitedPlayer.get(), invitationCreated(game, teamInvitation));
                        break;
                    case REJECT:
                        if (!invitation.isPresent()) {
                            sendMessage(channel, game, error("invitation not found"));
                            return;
                        }
                        if (invitedPlayer.get().getPlayerState() != PlayerState.AWAITS_GAME
                            && invitedPlayer.get().getPlayerState() != PlayerState.AWAITING_INVITATION_DECISION) {
                            sendMessage(channel, game, rejectedByPlayer("invitedPlayer does not longer awaits game or invitation decision", teamInvitation));
                            return;
                        }
                        if (playerInvitation.isPresent()) {
                            invitedPlayer.get().getInvitations().remove(playerInvitation.get());
;                        }
                        game.getInvitations().remove(invitation.get());
                        if (invitedPlayer.get().getPlayerState() == PlayerState.AWAITING_INVITATION_DECISION) {
                            invitedPlayer.get().setPlayerState(PlayerState.AWAITS_GAME);
                        }
                        sendMessage(invitedPlayer.get().getChannel(), invitedPlayer.get(), rejectedByHost("rejected by host", teamInvitation));
                        sendMessage(channel, game, rejectedByHost("rejected by host", teamInvitation));
                        break;
                    case ACCEPT:
                        if (!invitation.isPresent() || !playerInvitation.isPresent()) {
                            sendMessage(channel, game, error("invitation not found"));
                            return;
                        }
                        if (invitedPlayer.get().getPlayerState() != PlayerState.AWAITS_GAME
                                && invitedPlayer.get().getPlayerState() != PlayerState.AWAITING_INVITATION_DECISION) {
                            sendMessage(channel, game, rejectedByPlayer("invitedPlayer does not longer awaits game or invitation decision", teamInvitation));
                            return;
                        }
                        invitedPlayer.get().getInvitations().remove(playerInvitation.get());
                        game.getInvitations().remove(invitation.get());
                        game.getPlayersInTeam().put(invitedPlayer.get().getChannel().id(), invitedPlayer.get());
                        invitedPlayer.get().setPlayerState(PlayerState.GAME_GUEST);
                        invitedPlayer.get().setGrantedColor(teamInvitation.getPreferredColor());
                        invitedPlayer.get().setHostChannel(channel);
                        serverState.moveFromWaitingForGamesToPlayersInGame(invitedPlayer.get().getChannel());
                        GeometricFootballResponse.Response teamInfo = playersInTeamMessageCreator.createTeamInfo(game);
                        sendMessage(game.getChannel(), game, teamInfo);
                        sendMessage(invitedPlayer.get().getChannel(), invitedPlayer.get(),
                                invitationResult(GeometricFootballResponse.InvitationResult.ACCEPTED, invitation.get()));
                        game.getPlayersInTeam().values().stream()
                                .forEach(g -> sendMessage(g.getChannel(), g, teamInfo));
                        //TODO: remove all invitations and send to hosts that this player rejects other invitations
                        break;
                }
                break;
            case FROM_PLAYER_TO_HOST:
                if (game.getPlayerState() != PlayerState.AWAITS_GAME
                        && game.getPlayerState() != PlayerState.AWAITING_INVITATION_DECISION) {
                    notValidStateAction(channel, game, request);
                    return;
                }
                final Optional<Game> host = serverState.findHostPlayer(teamInvitation.getGameHostName());
                if (!host.isPresent()) {
                    sendMessage(channel, game, rejectedByPlayer("host does not exists", teamInvitation));
                    return;
                }
                final Game hostGame = host.get();
                final Optional<Invitation> hostInvitation = findHostInvitation(hostGame.getInvitations(), teamInvitation);
                final Optional<Invitation> guestInvitation = findGuestInvitation(game.getInvitations(), teamInvitation);
                switch (teamInvitation.getTeamInvitationAction()) {
                    case CREATE:
                        if (game.getPlayerState() != PlayerState.AWAITS_GAME) {
                            notValidStateAction(channel, game, request);
                            return;
                        }
                        if (hostGame.getPlayerState() != PlayerState.GAME_HOST) {
                            sendMessage(channel, game, error("host not in hosting state"));
                            return;
                        }
                        if (hostInvitation.isPresent() || guestInvitation.isPresent()) {
                            sendMessage(channel, game, error("invitation already exists"));
                            return;
                        }
                        final Invitation newInvitation = Invitation.builder()
                                .creationTime(new Date().getTime())
                                .invitator(hostGame.getOwnerName())
                                .invitatorChannel(hostGame.getChannel())
                                .invitedPlayer(game.getOwnerName())
                                .invitedPlayerChannel(game.getChannel())
                                .preferredColor(teamInvitation.getPreferredColor())
                                .build();
                        game.getInvitations().add(newInvitation);
                        game.setPlayerState(PlayerState.AWAITING_INVITATION_DECISION);
                        hostGame.getInvitations().add(newInvitation);
                        sendMessage(channel, game, invitationCreated(hostGame, teamInvitation));
                        sendMessage(hostGame.getChannel(), hostGame, invitationCreated(hostGame, teamInvitation));
                        break;
                    case ACCEPT:
                        if (!hostInvitation.isPresent() || !guestInvitation.isPresent()) {
                            sendMessage(channel, game, error("invitation not found"));
                            return;
                        }
                        hostGame.getInvitations().remove(hostInvitation.get());
                        game.setPlayerState(PlayerState.GAME_GUEST);
                        game.setGrantedColor(hostInvitation.get().getPreferredColor());
                        game.getInvitations().remove(guestInvitation.get());
                        game.setHostChannel(hostGame.getChannel());
                        serverState.moveFromWaitingForGamesToPlayersInGame(channel);
                        GeometricFootballResponse.Response invitationAccepted = invitationResult(GeometricFootballResponse.InvitationResult.INVITATION_ACCEPTED_BY_GUEST, guestInvitation.get());
                        sendMessage(hostGame.getChannel(), hostGame, invitationAccepted);
                        hostGame.getPlayersInTeam().put(channel.id(), game);
                        GeometricFootballResponse.Response teamInfo = playersInTeamMessageCreator.createTeamInfo(hostGame);
                        sendMessage(hostGame.getChannel(), hostGame, teamInfo);
                        sendMessage(channel, game, invitationAccepted);
                        hostGame.getPlayersInTeam().values().stream()
                                .forEach(g -> sendMessage(g.getChannel(), g, teamInfo));
                        //TODO: remove all invitations and send that this player had rejected invitations
                        break;
                    case REJECT:
                        if (!hostInvitation.isPresent() || !guestInvitation.isPresent()) {
                            sendMessage(channel, game, error("invitation not found"));
                            return;
                        }
                        hostGame.getInvitations().remove(hostInvitation.get());
                        game.getInvitations().remove(guestInvitation.get());
                        if (game.getPlayerState() == PlayerState.AWAITING_INVITATION_DECISION
                                && guestInvitation.get().getInvitator().equals(hostGame.getOwnerName())) {
                            game.setPlayerState(PlayerState.AWAITS_GAME);
                        }
                        GeometricFootballResponse.Response invitationRejected = invitationResult(GeometricFootballResponse.InvitationResult.INVITATION_REJECTED_BY_GUEST, guestInvitation.get());
                        sendMessage(channel, game, invitationRejected);
                        sendMessage(hostGame.getChannel(), hostGame, invitationRejected);
                        break;
                }
                break;
        }
    }

    private Optional<Invitation> findHostInvitation(final List<Invitation> invitations,
                                                    final GeometricFootballRequest.TeamInvitation teamInvitation) {
        return invitations.stream()
                .filter(invitation -> invitation.getInvitedPlayer().equals(teamInvitation.getInvitedPlayer()))
                .findFirst();
    }

    private Optional<Invitation> findGuestInvitation(final List<Invitation> invitations,
                                                    final GeometricFootballRequest.TeamInvitation teamInvitation) {
        return invitations.stream()
                .filter(invitation -> invitation.getInvitator().equals(teamInvitation.getGameHostName()))
                .findFirst();
    }

    private GeometricFootballResponse.Response rejectedByPlayer(final String message,
                                                                final GeometricFootballRequest.TeamInvitation teamInvitation) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.INVITATION_RESULT)
                .setTeamInvitationResponse(GeometricFootballResponse.TeamInvitationResponse.newBuilder()
                        .setInvitationResult(GeometricFootballResponse.InvitationResult.REJECTED)
                        .setMessage(message)
                        .setId(teamInvitation.getId())
                        .build())
                .build();
    }

    private GeometricFootballResponse.Response invitationCreated(final Game host,
                                                                 final GeometricFootballRequest.TeamInvitation teamInvitation) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.INVITATION_RESULT)
                .setTeamInvitationResponse(GeometricFootballResponse.TeamInvitationResponse.newBuilder()
                        .setInvitationResult(GeometricFootballResponse.InvitationResult.CREATED)
                        .setMessage("invitation created")
                        .setId(teamInvitation.getId())
                        .setGameHostName(host.getOwnerName())
                        .setInvitedPlayer(teamInvitation.getInvitedPlayer())
                        .build())
                .build();
    }

    private GeometricFootballResponse.Response rejectedByHost(final String message,
                                                                final GeometricFootballRequest.TeamInvitation teamInvitation) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.INVITATION_RESULT)
                .setTeamInvitationResponse(GeometricFootballResponse.TeamInvitationResponse.newBuilder()
                        .setInvitationResult(GeometricFootballResponse.InvitationResult.REJECTED)
                        .setMessage(message)
                        .setId(teamInvitation.getGameHostName())
                        .setGameHostName(teamInvitation.getGameHostName())
                        .setInvitedPlayer(teamInvitation.getInvitedPlayer())
                        .build())
                .build();
    }

    private GeometricFootballResponse.Response invitationResult(final GeometricFootballResponse.InvitationResult invitationResult,
                                                                 final Invitation invitation) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.INVITATION_RESULT)
                .setTeamInvitationResponse(GeometricFootballResponse.TeamInvitationResponse.newBuilder()
                        .setInvitationResult(invitationResult)
                        .setMessage("invitation created")
                        .setId(invitation.getInvitedPlayer())
                        .setGameHostName(invitation.getInvitator())
                        .setInvitedPlayer(invitation.getInvitedPlayer())
                        .setGrantedColor(invitation.getPreferredColor())
                        .build())
                .build();
    }
}
