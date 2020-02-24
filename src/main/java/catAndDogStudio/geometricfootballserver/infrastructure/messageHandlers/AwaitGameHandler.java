package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SelectableChannel;
import java.util.Set;

//@Service
@RequiredArgsConstructor
@Slf4j
public class AwaitGameHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final ChannelGroup waitingForGames;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        if (serverState.getWaitingForGamesOld().get(channel) != null) {
            sendMessage(channel, game, OutputMessages.CUTE_KITTY_YOU_ARE_ALREADY_WAITING_FOR_A_GAME);
            return;
        }
        game.setPlayerState(PlayerState.AWAITS_GAME);
        game.setPreferredColor(splittedMessage[2]);
        serverState.getWaitingForGamesOld().put(channel, game);
        sendMessage(channel, game, OutputMessages.AWAITING_FOR_GAME + ";" + game.getOwnerName());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return PlayerState.AWAITS_GAME.possibleStatesForTransition;
    }
}
