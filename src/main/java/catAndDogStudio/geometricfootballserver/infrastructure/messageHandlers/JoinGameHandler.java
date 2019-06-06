package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class JoinGameHandler extends BaseMessageHandler {
    private final ServerState serverState;

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        SelectableChannel hostChannel = serverState.getHostPlayer(splittedMessage[1]);
        if (hostChannel == null) {
            sendMessage(channel, game, OutputMessages.NO_SUCH_HOSTING_KITTY + ";" + splittedMessage[1]);
            return;
        }
        Game hostGame = serverState.getHostedGames().get(hostChannel);
        hostGame.getInvitations().add(Invitation.builder()
                .creationTime(new Date().getTime())
                .invitator(hostGame.getOwnerName())
                .invitatorChannel(hostChannel)
                .invitedPlayer(game.getOwnerName())
                .invitedPlayerChannel(channel)
                .build());
        sendMessage(hostChannel, hostGame, OutputMessages.INVITATION + ";" + game.getOwnerName());
        game.getInvitations().add(Invitation.builder()
                .creationTime(new Date().getTime())
                .invitator(hostGame.getOwnerName())
                .invitatorChannel(hostChannel)
                .invitedPlayer(game.getOwnerName())
                .invitedPlayerChannel(channel)
                .build());
        game.setPlayerState(PlayerState.AWAITING_INVITATION_DECISION);
        sendMessage(channel, game, OutputMessages.INVITATION_SENT + ";" + hostGame.getOwnerName());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return PlayerState.AWAITING_INVITATION_DECISION.possibleStatesForTransition;
    }
}
