package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageHandlingStrategy {

    private final GetPlayersHandler getPlayersHandler;
    private final InvitePlayerHandler invitePlayerHandler;
    private final AuthenticationHandler authenticationHandler;
    private final HostGameHandler hostGameHandler;
    private final AwaitGameHandler awaitGameHandler;
    private final LeaveGameHandler leaveGameHandler;
    private final JoinGameHandler joinGameHandler;
    private final CancelInvitationHandler cancelInvitationHandler;
    private final InvitationAcceptedByHostHandler invitationAcceptedByHostHandler;
    private final InvitationRejectedByHostHandler invitationRejectedByHostHandler;
    private final InvitationAcceptedByGuestHandler invitationAcceptedByGuestHandler;
    private final InvitationRejectedByGuestHandler invitationRejectedByGuestHandler;
    private final PlayerKickedOutByHostHandler playerKickedOutByHostHandler;
    private final PingPongHandler pingPongHandler;
    private final SetTacticHandler setTacticHandler;
    private final SetTeamHandler setTeamHandler;
    private final SetTeamPlayersHandler setTeamPlayersHandler;
    private final SetTacticMappingHandler setTacticMappingHandler;
    private Map<String, GeometricServerMessageHandler> handlers = new HashMap<>();

    @PostConstruct
    public void setHandlers() {
        handlers.put(InputMessages.GET_PLAYERS, getPlayersHandler);
        handlers.put(InputMessages.INVITE_PLAYER, invitePlayerHandler);
        handlers.put(InputMessages.MAU, authenticationHandler);
        handlers.put(InputMessages.HOST_GAME, hostGameHandler);
        handlers.put(InputMessages.AWAIT_GAME, awaitGameHandler);
        handlers.put(InputMessages.LEAVE_GAME, leaveGameHandler);
        handlers.put(InputMessages.JOIN_GAME, joinGameHandler);
        handlers.put(InputMessages.CANCEL_INVITATION, cancelInvitationHandler);
        handlers.put(InputMessages.INVITATION_ACCEPTED_BY_HOST, invitationAcceptedByHostHandler);
        handlers.put(InputMessages.INVITATION_REJECTED_BY_HOST, invitationRejectedByHostHandler);
        handlers.put(InputMessages.INVITATION_ACCEPTED_BY_GUEST, invitationAcceptedByGuestHandler);
        handlers.put(InputMessages.INVITATION_REJECTED_BY_GUEST, invitationRejectedByGuestHandler);
        handlers.put(InputMessages.KITTY_KICKED_BY_HOST_KITTY, playerKickedOutByHostHandler);
        handlers.put(InputMessages.PING, pingPongHandler);
        handlers.put(InputMessages.SET_TACTIC, setTacticHandler);
        handlers.put(InputMessages.SET_TEAM, setTeamHandler);
        handlers.put(InputMessages.SET_TACTIC_MAPPING, setTacticMappingHandler);
        handlers.put(InputMessages.SET_TEAM_PLAYERS, setTeamPlayersHandler);
    }

    public void handleMessage(SelectableChannel channel, Game game, String message) {
        log.debug("message received from " + game.getOwnerName() + " " + message);
        final String[] splittedMessage;
        try {
            splittedMessage = message.split(";");
        } catch (Exception e) {
            log.warn("error while splitting message, sender: {}, message: {}, error: {} {}",
                    game.getOwnerName(), message, e.getMessage(), e.getStackTrace());
            return;
        }
        GeometricServerMessageHandler handler = handlers.get(splittedMessage[0]);
        if (handler != null) {
            handler.handleMessage(channel, game, splittedMessage);
            return;
        }

        log.warn("no valid strategy found for message, sender: {}, message: {}", game.getOwnerName(), message);
    }
}
