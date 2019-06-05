package catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitePlayerHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = Stream.of(PlayerState.GAME_HOST)
            .collect(Collectors.toSet());

    @Override
    public void handleMessage(SelectableChannel channel, Game game, String[] splittedMessage) {
        String invitedPlayer = splittedMessage[1];
        Invitation invitation = game.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(invitedPlayer))
                .findFirst()
                .orElse(null);
        if (invitation != null) {
            if (serverState.getWaitingForGames().containsKey(invitation.getInvitedPlayerChannel())) {
                sendMessage(channel, game, OutputMessages.ALREADY_INVITED + ";" + invitedPlayer);
                return;
            } else {
                game.getInvitations().remove(invitation);
            }
        }
        final SelectableChannel invitedPlayerChannel = serverState.getWaitingPlayer(invitedPlayer);
        if (invitedPlayerChannel == null) {
            sendMessage(channel, game, OutputMessages.PLAYER_UNAVAILABLE + ";" + invitedPlayer);
            return;
        }
        game.getInvitations().add(
                Invitation.builder()
                        .creationTime(new Date().getTime())
                        .invitator(game.getOwnerName())
                        .invitedPlayer(invitedPlayer)
                        .invitatorChannel(channel)
                        .invitedPlayerChannel(invitedPlayerChannel)
                        .build());
        sendMessage(invitedPlayerChannel, game, OutputMessages.INVITATION + ";" + game.getOwnerName());
        sendMessage(channel, game, "INVITATION_SENT" + ";" + invitedPlayer);
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }
}