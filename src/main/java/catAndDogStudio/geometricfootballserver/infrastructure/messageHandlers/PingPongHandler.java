package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;

@Service
@RequiredArgsConstructor
@Slf4j
public class PingPongHandler extends BaseMessageHandler {
    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        sendMessage(channel, game, OutputMessages.PONG + ";" + game.getOwnerName());
    }
}
