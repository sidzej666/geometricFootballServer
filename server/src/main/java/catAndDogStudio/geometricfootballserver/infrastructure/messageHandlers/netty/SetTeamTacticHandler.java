package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import com.cat_and_dog_studio.geometric_football.protocol.Model;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetTeamTacticHandler extends BaseMessageHandler {

    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final Model.Tactic tactic = request.getTactic();
        game.setTactic(tactic);
        sendMessage(channel, game, ok());
        game.getPlayersInTeam().values().stream()
                .forEach(g -> sendTactic(g, tactic));
    }

    private void sendTactic(final Game game, final Model.Tactic tactic) {
        sendMessage(game.getChannel(), game, tacticResponse(tactic));
    }

    private GeometricFootballResponse.Response tacticResponse(final Model.Tactic tactic) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.SET_TACTIC)
                .setTactic(tactic)
                .build();
    }

    private GeometricFootballResponse.Response ok() {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.OK)
                .setOkResponse(GeometricFootballResponse.OkResponse.newBuilder()
                        .setMessage("team tactic set")
                        .build())
                .build();
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
