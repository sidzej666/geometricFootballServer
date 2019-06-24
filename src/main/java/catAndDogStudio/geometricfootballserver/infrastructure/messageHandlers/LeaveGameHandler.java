package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.LeaveGameService;
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
    private final LeaveGameService leaveGameService;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST, PlayerState.GAME_GUEST,
            PlayerState.AWAITS_GAME, PlayerState.AWAITING_INVITATION_DECISION);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        leaveGameService.leaveGame(channel, game, true);
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}
