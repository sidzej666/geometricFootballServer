package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
import org.springframework.stereotype.Service;

@Service
public class PlayersInTeamMessageCreator {
    private final static String PLAYER = "PLAYER";
    private final static String INVITATION = "INVITATION";
    private final static String HOST = "HOST";
    public String message(final Game game, final String messageType) {
        final StringBuilder stringBuilder = new StringBuilder(messageType);
        stringBuilder.append(Constants.MESSAGE_SEPARATOR);
        stringBuilder.append(game.getOwnerName());
        stringBuilder.append(Constants.SUB_MESSAGE_SEPARATOR);
        stringBuilder.append(HOST);
        stringBuilder.append(Constants.SUB_MESSAGE_SEPARATOR);
        stringBuilder.append(game.getGrantedColor());
        for(Game playerInGame : game.getPlayersInGame().values()) {
            stringBuilder.append(Constants.MESSAGE_SEPARATOR);
            stringBuilder.append(playerInGame.getOwnerName());
            stringBuilder.append(Constants.SUB_MESSAGE_SEPARATOR);
            stringBuilder.append(PLAYER);
            stringBuilder.append(Constants.SUB_MESSAGE_SEPARATOR);
            stringBuilder.append(playerInGame.getGrantedColor());
        }
        for(Invitation invitation : game.getInvitations()) {
            stringBuilder.append(Constants.MESSAGE_SEPARATOR);
            stringBuilder.append(invitation.getInvitedPlayer());
            stringBuilder.append(Constants.SUB_MESSAGE_SEPARATOR);
            stringBuilder.append(INVITATION);
            stringBuilder.append(Constants.SUB_MESSAGE_SEPARATOR);
            stringBuilder.append(invitation.getPreferredColor());
        }
        return stringBuilder.toString();
    }
}

