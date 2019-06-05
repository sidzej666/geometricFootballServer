package catAndDogStudio.geometricfootballserver.infrastructure;

import catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers.GeometricServerMessageHandler;
import catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers.GetPlayersHandler;
import catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers.InputMessages;
import catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers.InvitePlayerHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageHandlingStrategy {

    private final GetPlayersHandler getPlayersHandler;
    private final InvitePlayerHandler invitePlayerHandler;
    private Map<String, GeometricServerMessageHandler> handlers = new HashMap<>();

    @PostConstruct
    public void setHandlers() {
        handlers.put(InputMessages.GET_PLAYERS, getPlayersHandler);
        handlers.put(InputMessages.INVITATION, invitePlayerHandler);
    }

    public void handleMessage(SelectableChannel channel, Game game, String message) {
        final String[] splittedMessage;
        try {
            splittedMessage = message.split(";");
        } catch (Exception e) {
            log.warn("error while splitting message, sender: {}, message: {}, error: {} {}",
                    game.getOwnerName(), message, e.getMessage(), e.getStackTrace());
            return;
        }
        GeometricServerMessageHandler handler = handlers.get(splittedMessage[0]);
        if (handler != null) {
            handler.handleMessage(channel, game, splittedMessage);
            return;
        }

        log.warn("no valid strategy found for message, sender: {}, message: {}", game.getOwnerName(), message);
    }
}
