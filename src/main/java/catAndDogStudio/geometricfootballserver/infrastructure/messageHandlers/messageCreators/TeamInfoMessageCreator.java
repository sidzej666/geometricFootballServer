package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.MessageSender;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.stream.Collectors;

@Service
public class TeamInfoMessageCreator extends MessageSender {
    public String teamPlayersMessage(Game game) {
        return OutputMessages.TEAM_TEAM_PLAYERS + Constants.MESSAGE_SEPARATOR + game.getPlayers();
    }
    public String teamTacticMessage(Game game) {
        return OutputMessages.TEAM_TACTIC + Constants.MESSAGE_SEPARATOR + game.getTactic();
    }
    public String teamTacticMappingMessage(Game game) {
        return OutputMessages.TEAM_TACTIC_MAPPING + Constants.MESSAGE_SEPARATOR + game.getTacticMapping();
    }
    public String teamTeamMessage(Game game) {
        return OutputMessages.TEAM_TEAM + Constants.MESSAGE_SEPARATOR + game.getTeam();
    }
    public String teamPlayersUsersMapping(Game game) {
        return OutputMessages.TEAM_PLAYERS_USERS_MAPPING + Constants.MESSAGE_SEPARATOR +
                game.getPlayersUsersMapping().keySet().stream()
                    .map(k -> k + Constants.SUB_MESSAGE_SEPARATOR_FOR_IDS + game.getPlayersUsersMapping().get(k))
                    .collect(Collectors.joining(Constants.MESSAGE_SEPARATOR));
    }
    public void sendAllAvailableTeamInfo(Game hostGame, SelectableChannel guestChannel, Game guestGame) {
        if (hostGame.getTeam() != null) {
            sendMessage(guestChannel, guestGame, teamTeamMessage(hostGame));
        }
        if (hostGame.getTactic() != null) {
            sendMessage(guestChannel, guestGame, teamTacticMessage(hostGame));
        }
        if (hostGame.getTacticMapping() != null) {
            sendMessage(guestChannel, guestGame, teamTacticMappingMessage(hostGame));
        }
        if (hostGame.getPlayers() != null) {
            sendMessage(guestChannel, guestGame, teamPlayersMessage(hostGame));
        }
        if (!hostGame.getPlayersUsersMapping().isEmpty()) {
            sendMessage(guestChannel, guestGame, teamPlayersUsersMapping(hostGame));
        }
    }
}
