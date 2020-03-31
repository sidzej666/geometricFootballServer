package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.TeamInfoMessageCreator;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty.BaseMessageHandler;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadyForGameService  extends BaseMessageHandler {
    private final ServerState serverState;
    private final TeamInfoMessageCreator teamInfoMessageCreator;
    private final ActiveGameService activeGameFactory;

    public void ifPossibleFindOpponentAndTransitionToInGame(final Game game) {
        final Optional<Game> opponent = serverState.findOpponent(game.getChannel().id());
        if (!opponent.isPresent()) {
            return;
        }
        serverState.moveFromWaitingForOpponentsToActiveGames(opponent.get().getChannel());
        serverState.moveFromWaitingForOpponentsToActiveGames(game.getChannel());
        game.setPlayerState(PlayerState.PLAYING);
        opponent.get().setPlayerState(PlayerState.PLAYING);
        sentOpponentData(game, opponent.get());
        sentOpponentData(opponent.get(), game);
        //TODO?
        activeGameFactory.createActiveGameAndSetUpGameObjects(game, opponent.get());
    }

    private void sentOpponentData(final Game game, final Game opponent) {
        sendOpponentDataToSingleChannel(game, opponent);
        game.getPlayersInTeam().values()
                .forEach(playerInGame -> sendOpponentDataToSingleChannel(playerInGame, opponent));
    }
    private void sendOpponentDataToSingleChannel(final Game game, final Game opponent) {
        sendMessage(game.getChannel(), game, opponentFound(opponent));
        teamInfoMessageCreator.sendAllAvailableTeamInfo(game, opponent, true, false);
    }

    private GeometricFootballResponse.Response opponentFound(final Game opponent) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.OPPONENT_FOUND)
                .setOpponentFound(GeometricFootballResponse.OpponentFound.newBuilder()
                        .setName(opponent.getOwnerName())
                        .build())
                .build();
    }
}
