package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.InputMessages;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;

@Slf4j
public class ClientResponses {
    public static String joinRequestSentToHost(String hostName) {
        return OutputMessages.INVITATION_SENT + Constants.MESSAGE_SEPARATOR + hostName;
    }
    public static String joinRequestReceivedByHost(String guestName, String preferredColor) {
        return OutputMessages.INVITATION + Constants.MESSAGE_SEPARATOR + guestName
                + Constants.MESSAGE_SEPARATOR + preferredColor;
    }
    public static String noSuchHostingKitty(String hostName) {
        return OutputMessages.NO_SUCH_HOSTING_KITTY + Constants.MESSAGE_SEPARATOR + hostName;
    }
    public static String invitationNotFound(String guestName) {
        return OutputMessages.INVITATION_NOT_FOUND + Constants.MESSAGE_SEPARATOR + guestName;
    }
    public static String kittyJoinedGame(String guestName) {
        return OutputMessages.KITTY_JOINED_GAME + Constants.MESSAGE_SEPARATOR + guestName;
    }
    public static String playersList(String hostName, String hostColor, String... playersWithColors) {
        return OutputMessages.TEAM_PLAYERS + Constants.MESSAGE_SEPARATOR
                + hostName + Constants.SUB_MESSAGE_SEPARATOR + "HOST" + Constants.SUB_MESSAGE_SEPARATOR + hostColor + Constants.MESSAGE_SEPARATOR
                + generatePlayers(playersWithColors);
    }
    private static String generatePlayers(String[] playersWithColors) {
        StringBuilder players = new StringBuilder();
        for (int i = 0; i < playersWithColors.length; i += 2) {
            players.append(playersWithColors[i]);
            players.append(Constants.SUB_MESSAGE_SEPARATOR);
            players.append("PLAYER");
            players.append(Constants.SUB_MESSAGE_SEPARATOR);
            players.append(playersWithColors[i + 1]);
            if (i < playersWithColors.length - 2) {
                players.append(Constants.MESSAGE_SEPARATOR);
            }
        }
        return players.toString();
    }
    public static String playerMappings(String... playersIdsAndUsernames) {
        return OutputMessages.TEAM_PLAYERS_USERS_MAPPING + Constants.MESSAGE_SEPARATOR
                + ClientMessages.generatePlayersIdsAndUsernames(playersIdsAndUsernames);
    }
}
