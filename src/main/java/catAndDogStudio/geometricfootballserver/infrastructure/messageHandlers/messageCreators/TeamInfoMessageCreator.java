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
    public String teamPlayersMessage(Game game, String messageType) {
        return messageType + Constants.MESSAGE_SEPARATOR + game.getPlayers();
    }
    public String teamTacticMessage(Game game, String messageType) {
        return messageType + Constants.MESSAGE_SEPARATOR + game.getTactic();
    }
    public String teamTacticMappingMessage(Game game, String messageType) {
        return messageType + Constants.MESSAGE_SEPARATOR + game.getTacticMapping();
    }
    public String teamTeamMessage(Game game, String messageType) {
        return messageType + Constants.MESSAGE_SEPARATOR + game.getTeam();
    }
    public String teamPlayersUsersMapping(Game game, String messageType) {
        return messageType + Constants.MESSAGE_SEPARATOR +
                game.getPlayersUsersMapping().keySet().stream()
                    .map(k -> k + Constants.SUB_MESSAGE_SEPARATOR_FOR_IDS + game.getPlayersUsersMapping().get(k))
                    .collect(Collectors.joining(Constants.MESSAGE_SEPARATOR));
    }
    public void sendAllAvailableTeamInfo(Game hostGame, SelectableChannel guestChannel, Game guestGame) {
        if (hostGame.getTeam() != null) {
            sendMessage(guestChannel, guestGame, teamTeamMessage(hostGame, OutputMessages.TEAM_TEAM));
        }
        if (hostGame.getTactic() != null) {
            sendMessage(guestChannel, guestGame, teamTacticMessage(hostGame, OutputMessages.TEAM_TACTIC));
        }
        if (hostGame.getTacticMapping() != null) {
            sendMessage(guestChannel, guestGame, teamTacticMappingMessage(hostGame, OutputMessages.TEAM_TACTIC_MAPPING));
        }
        if (hostGame.getPlayers() != null) {
            sendMessage(guestChannel, guestGame, teamPlayersMessage(hostGame, OutputMessages.TEAM_TEAM_PLAYERS));
        }
        if (!hostGame.getPlayersUsersMapping().isEmpty()) {
            sendMessage(guestChannel, guestGame, teamPlayersUsersMapping(hostGame, OutputMessages.TEAM_PLAYERS_USERS_MAPPING));
        }
    }
    public void sendAllAvailableOpponentTeamInfo(Game opponentGame, SelectableChannel channel, Game game) {
        if (opponentGame.getTeam() != null) {
            sendMessage(channel, game, teamTeamMessage(opponentGame, OutputMessages.OPPONENT_TEAM_TEAM));
        }
        if (opponentGame.getTactic() != null) {
            sendMessage(channel, game, teamTacticMessage(opponentGame, OutputMessages.OPPONENT_TEAM_TACTIC));
        }
        if (opponentGame.getTacticMapping() != null) {
            sendMessage(channel, game, teamTacticMappingMessage(opponentGame, OutputMessages.OPPONENT_TEAM_TACTIC_MAPPING));
        }
        if (opponentGame.getPlayers() != null) {
            sendMessage(channel, game, teamPlayersMessage(opponentGame, OutputMessages.OPPONENT_TEAM_TEAM_PLAYERS));
        }
        if (!opponentGame.getPlayersUsersMapping().isEmpty()) {
            sendMessage(channel, game, teamPlayersUsersMapping(opponentGame, OutputMessages.OPPONENT_TEAM_PLAYERS_USERS_MAPPING));
        }
    }
}
