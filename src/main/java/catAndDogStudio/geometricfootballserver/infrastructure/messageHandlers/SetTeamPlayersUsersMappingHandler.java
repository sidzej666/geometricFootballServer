package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators.TeamInfoMessageCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetTeamPlayersUsersMappingHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);
    private final TeamInfoMessageCreator teamInfoMessageCreator;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        for(int i = 1; i < splittedMessage.length; i++) {
            final String[] playerIdAndUserName = splittedMessage[i].split(Constants.SUB_MESSAGE_SEPARATOR_FOR_IDS);
            final String playerId = playerIdAndUserName[0];
            final String userName = playerIdAndUserName[1];
            game.getPlayersUsersMapping().put(playerId, userName);
        }
        game.getPlayersInGame().keySet().stream()
                .forEach(c -> sendPlayerUsersMapping(c, game.getPlayersInGame().get(c), game));
    }

    private void sendPlayerUsersMapping(SelectableChannel c, Game guestGame, Game hostGame) {
        sendMessage(c, guestGame, teamInfoMessageCreator.teamPlayersUsersMapping(hostGame));
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
