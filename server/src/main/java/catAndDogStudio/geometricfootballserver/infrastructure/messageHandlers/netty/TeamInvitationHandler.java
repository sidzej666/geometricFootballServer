package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.InvitationService;
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
    private final InvitationService invitationService;

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
                    sendMessage(channel, game, invitationService.rejectedByPlayer("invitedPlayer does not exists", teamInvitation.getId()));
                    return;
                }
                final Optional<Invitation> playerInvitation = invitationService.findGuestInvitation(invitedPlayer.get().getInvitations(), teamInvitation.getGameHostName());
                switch (teamInvitation.getTeamInvitationAction()) {
                    case CREATE:
                        if (invitation.isPresent()) {
                            sendMessage(channel, game, error("invitation already exists"));
                            return;
                        }
                        if (invitedPlayer.get().getPlayerState() != PlayerState.AWAITS_GAME) {
                            sendMessage(channel, game, invitationService.rejectedByPlayer("invitedPlayer does not longer awaits game", teamInvitation.getId()));
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
                        invitationService.cancelInvitation(invitation.get(), game, true);
                        break;
                    case ACCEPT:
                        if (!invitation.isPresent() || !playerInvitation.isPresent()) {
                            sendMessage(channel, game, error("invitation not found"));
                            return;
                        }
                        if (invitedPlayer.get().getPlayerState() != PlayerState.AWAITS_GAME
                                && invitedPlayer.get().getPlayerState() != PlayerState.AWAITING_INVITATION_DECISION) {
                            sendMessage(channel, game, invitationService.rejectedByPlayer("invitedPlayer does not longer awaits game or invitation decision", teamInvitation.getId()));
                            return;
                        }
                        invitedPlayer.get().getInvitations().remove(playerInvitation.get());
                        game.getInvitations().remove(invitation.get());
                        game.getPlayersInTeam().put(invitedPlayer.get().getChannel().id(), invitedPlayer.get());
                        invitedPlayer.get().setPlayerState(PlayerState.GAME_GUEST);
                        invitedPlayer.get().setGrantedColor(teamInvitation.getPreferredColor());
                        invitedPlayer.get().setHostChannel(channel);
                        serverState.moveFromWaitingForGamesToPlayersInGame(invitedPlayer.get().getChannel());
                        GeometricFootballResponse.Response teamInfo = playersInTeamMessageCreator.createTeamInfo(game, false, true);
                        sendMessage(game.getChannel(), game, teamInfo);
                        sendMessage(invitedPlayer.get().getChannel(), invitedPlayer.get(),
                                invitationResult(GeometricFootballResponse.InvitationResult.ACCEPTED, invitation.get()));
                        game.getPlayersInTeam().values().stream()
                                .forEach(g -> sendMessage(g.getChannel(), g, teamInfo));
                        invitationService.removePendingGuestInvitationsAndInformHosts(invitedPlayer.get());
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
                    sendMessage(channel, game, invitationService.rejectedByPlayer("host does not exists", teamInvitation.getId()));
                    return;
                }
                final Game hostGame = host.get();
                final Optional<Invitation> hostInvitation = findHostInvitation(hostGame.getInvitations(), teamInvitation);
                final Optional<Invitation> guestInvitation = invitationService.findGuestInvitation(game.getInvitations(), teamInvitation.getGameHostName());
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
                        GeometricFootballResponse.Response teamInfo = playersInTeamMessageCreator.createTeamInfo(hostGame, false, true);
                        sendMessage(hostGame.getChannel(), hostGame, teamInfo);
                        sendMessage(channel, game, invitationAccepted);
                        hostGame.getPlayersInTeam().values().stream()
                                .forEach(g -> sendMessage(g.getChannel(), g, teamInfo));
                        invitationService.removePendingGuestInvitationsAndInformHosts(game);
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
