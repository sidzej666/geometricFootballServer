package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Date;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationHandler extends BaseMessageHandler {

    @Override
    protected void messageAction(SelectableChannel channel, Game game, String[] splittedMessage) {
        String key = splittedMessage[1];
        if ("1234567890".equals(key)) {
            game.setOwnerName("kotek-maskotek");
        } else if ("0987654321".equals(key)) {
            game.setOwnerName("java-kotek");
        } else if ("1".equals(key)) {
            game.setOwnerName("piesek");
        } else if ("2".equals(key)) {
            game.setOwnerName("mysio");
        } else {
            sendMessage(channel, game, OutputMessages.YOU_ARE_MAUING_IN_A_VERY_STRANGE_WAY_KITTY);
            return;
        }
        game.setPlayerState(PlayerState.AUTHENTICATED);
        game.setLastHearthBeatTime(new Date().getTime());
        sendMessage(channel, game, OutputMessages.VERY_CUTE_MAU_HELLO_KITTY + ";" + game.getOwnerName());
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return PlayerState.AUTHENTICATED.possibleStatesForTransition;
    }
}
