package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.LeaveGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Set;

//@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveGameHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final LeaveGameService leaveGameService;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        leaveGameService.leaveGame(null, game, true);
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return leaveGameService.getAllowedStates();
    }
}
