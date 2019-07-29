package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerKickedOutByHostHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> availableStates = EnumSet.of(PlayerState.GAME_HOST);

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        String guestName = splittedMessage[1];
        SelectableChannel guestChannel = game.getPlayersInGame().keySet().stream()
                .filter(k -> game.getPlayersInGame().get(k).getOwnerName().equals(guestName))
                .findAny()
                .orElse(null);
        if (guestChannel == null) {
            sendMessage(channel, game, OutputMessages.NO_SUCH_KITTY_IN_GAME_KUWETA + ";" + guestName);
            return;
        }
        Game guestGame = game.getPlayersInGame().get(guestChannel);
        serverState.getWaitingForGames().put(guestChannel, guestGame);
        guestGame.setPlayerState(PlayerState.AWAITS_GAME);
        sendPlayerKickedOutToAllPlayersInGame(game, guestName);
        game.getPlayersInGame().remove(guestChannel);
        sendMessage(channel, game, OutputMessages.KITTY_KICKED_OUT + ";" + guestName);
    }

    private void sendPlayerKickedOutToAllPlayersInGame(Game game, String guestName) {
        for (SelectableChannel channel: game.getPlayersInGame().keySet()) {
            sendMessage(channel, game.getPlayersInGame().get(channel), OutputMessages.KITTY_KICKED_OUT + ";" + guestName);
        }
    }
    @Override
    protected Set<PlayerState> getPossibleStates() {
        return availableStates;
    }
}
