package catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;

import java.nio.channels.SelectableChannel;

public interface GeometricServerMessageHandler {
    void handleMessage(SelectableChannel channel, Game game, String[] splittedMessage);
}
