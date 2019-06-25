package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import javafx.beans.binding.StringBinding;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetTeamPlayersHandler extends BaseMessageHandler{
    private final ServerState serverState;
    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        final StringBuilder players = new StringBuilder("");
        for(int i = 1; i < splittedMessage.length; i++) {
            players.append(splittedMessage[i]);
            if (i < splittedMessage.length - 1) {
                players.append(Constants.MESSAGE_SEPARATOR);
            }
        }
        game.setPlayers(players.toString());
        game.getPlayersInGame().keySet().stream()
                .forEach(c -> sendTeam(c, game.getPlayersInGame().get(c), game.getPlayers()));
    }

    private void sendTeam(SelectableChannel c, Game guestGame, String players) {
        sendMessage(c, guestGame, OutputMessages.TEAM_TEAM_PLAYERS + Constants.MESSAGE_SEPARATOR + players);
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
