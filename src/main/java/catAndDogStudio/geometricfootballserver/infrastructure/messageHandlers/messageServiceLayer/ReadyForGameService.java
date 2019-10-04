package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.MessageSender;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.PlayersInTeamMessageCreator;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.TeamInfoMessageCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadyForGameService  extends MessageSender {
    private final ServerState serverState;
    private final TeamInfoMessageCreator teamInfoMessageCreator;
    private final PlayersInTeamMessageCreator playersInTeamMessageCreator;
    private final ActiveGameService activeGameFactory;

    public void transitionServerStateAndFindOpponent(SelectableChannel channel, Game game) {
        serverState.getHostedGames().remove(channel);
        SelectableChannel opponentChannel = serverState.getTeamsWaitingForOpponents().keySet().stream()
                .min(Comparator.comparing(oc -> serverState.getTeamsWaitingForOpponents().get(oc).getReadyForGameTime()))
                .orElse(null);
        Game opponent = serverState.getTeamsWaitingForOpponents().get(opponentChannel);
        if (opponent != null) {
            serverState.getPlayersInGame().put(channel, game);
            serverState.getPlayersInGame().put(opponent.getHostChannel(), opponent);
            serverState.getTeamsWaitingForOpponents().remove(opponent.getHostChannel());
        } else {
            serverState.getTeamsWaitingForOpponents().put(channel, game);
            return;
        }
        game.setPlayerState(PlayerState.PLAYING);
        opponent.setPlayerState(PlayerState.PLAYING);
        sentOpponentData(game, channel, opponent);
        sentOpponentData(opponent, opponentChannel, game);
        activeGameFactory.createActiveGameAndSetUpGameObjects(game, opponent);

    }
    public void goBackToTeamCreationServerTransition(SelectableChannel channel, Game game) {
        serverState.getHostedGames().put(channel, game);
        serverState.getTeamsWaitingForOpponents().remove(channel);
    }

    private void sentOpponentData(Game targetGame, SelectableChannel targetGameChannel, Game opponent) {
        sendOpponentDataToSingleChannel(opponent, targetGameChannel, targetGame);
        targetGame.getPlayersInGame().keySet()
                .stream()
                .forEach(k -> sendOpponentDataToSingleChannel(opponent, k, targetGame.getPlayersInGame().get(k)));
    }
    private void sendOpponentDataToSingleChannel(Game opponent, SelectableChannel targetGameChannel, Game targetGame) {
        sendMessage(targetGameChannel, targetGame, playersInTeamMessageCreator.message(opponent, OutputMessages.OPPONENT_TEAM_PLAYERS));
        sendMessage(targetGameChannel, targetGame, OutputMessages.getOpponentFoundMessage(opponent.getOwnerName()));
        teamInfoMessageCreator.sendAllAvailableOpponentTeamInfo(opponent, targetGameChannel, targetGame);
    }
}
