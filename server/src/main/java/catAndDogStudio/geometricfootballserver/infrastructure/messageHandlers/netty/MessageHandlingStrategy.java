package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest.RequestType;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageHandlingStrategy {

    private final AuthenticationHandler authenticationHandler;
    private final DisconnectionHandler disconnectionHandler;
    private final HostGameHandler hostGameHandler;
    private final AwaitGameHandler awaitGameHandler;
    private final GetPlayersHandler getPlayersHandler;
    private final TeamInvitationHandler teamInvitationHandler;
    private final KickPlayerHandler kickPlayerHandler;
    private final LeaveTeamHandler leaveTeamHandler;
    private final SetTeamHandler setTeamHandler;
    private final SetTeamTacticHandler setTeamTacticHandler;
    private final SetTeamTacticMappingHandler setTeamTacticMappingHandler;
    private final SetTeamPlayersHandler setTeamPlayersHandler;
    private final SetTeamPlayersUsersMappingHandler setTeamPlayersUsersMappingHandler;
    private final ReadyForGameHandler readyForGameHandler;
    private Map<RequestType, GeometricServerMessageHandler> handlers = new HashMap<>();

    @PostConstruct
    public void setHandlers() {
        handlers.put(RequestType.AUTHENTICATION, authenticationHandler);
        handlers.put(RequestType.DISCONNECT, disconnectionHandler);
        handlers.put(RequestType.HOST_GAME, hostGameHandler);
        handlers.put(RequestType.AWAIT_GAME, awaitGameHandler);
        handlers.put(RequestType.GET_PLAYERS, getPlayersHandler);
        handlers.put(RequestType.TEAM_INVITATION, teamInvitationHandler);
        handlers.put(RequestType.KICK_PLAYER, kickPlayerHandler);
        handlers.put(RequestType.LEAVE_TEAM, leaveTeamHandler);
        handlers.put(RequestType.SET_TEAM, setTeamHandler);
        handlers.put(RequestType.SET_TACTIC, setTeamTacticHandler);
        handlers.put(RequestType.SET_TACTIC_MAPPING, setTeamTacticMappingHandler);
        handlers.put(RequestType.SET_TEAM_PLAYERS, setTeamPlayersHandler);
        handlers.put(RequestType.SET_TEAM_PLAYERS_USERS_MAPPING, setTeamPlayersUsersMappingHandler);
        handlers.put(RequestType.READY_FOR_GAME, readyForGameHandler);
    }

    public void handleMessage(final Channel channel, final Game game,
                              final GeometricFootballRequest.Request request) {
        if (request.getType() == RequestType.PING) {
            return;
        }
        log.debug("message received from " + game.getOwnerName() + " " + request);

        GeometricServerMessageHandler handler = handlers.get(request.getType());
        if (handler != null) {
            handler.handleMessage(channel, game, request);
            return;
        }

        log.warn("no valid strategy found for message, sender: {}, message: {}", game.getOwnerName(), request);
    }
}
