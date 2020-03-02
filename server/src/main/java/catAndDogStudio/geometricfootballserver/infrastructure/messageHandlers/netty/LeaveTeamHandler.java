package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.LeaveGameService;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveTeamHandler extends BaseMessageHandler {

    private final LeaveGameService leaveGameService;

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        leaveGameService.leaveGame(channel, game, false);
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return leaveGameService.getAllowedStates();
    }
}
