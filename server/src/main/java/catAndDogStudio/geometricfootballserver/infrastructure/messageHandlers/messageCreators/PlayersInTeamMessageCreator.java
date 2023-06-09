package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.Invitation;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
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

    public GeometricFootballResponse.Response createTeamInfo(final Game game, final boolean opponent,
                                                             final boolean withInvitations) {
        final GeometricFootballResponse.TeamPlayersResponse.Builder teamPlayers = GeometricFootballResponse.TeamPlayersResponse.newBuilder();
        teamPlayers.addTeamPlayerData(0, createTeamPlayer(game, GeometricFootballResponse.TeamPlayerType.HOST));
        int i = 1;
        for(final Game playerInGame : game.getPlayersInTeam().values()) {
            teamPlayers.addTeamPlayerData(i, createTeamPlayer(playerInGame, GeometricFootballResponse.TeamPlayerType.PLAYER));
            i++;
        }
        if (withInvitations) {
            for(final Invitation invitation : game.getInvitations()) {
                teamPlayers.addTeamPlayerData(i, createTeamPlayer(invitation, GeometricFootballResponse.TeamPlayerType.INVITATION));
                i++;
            }
        }
        return GeometricFootballResponse.Response.newBuilder()
                .setType(opponent ? GeometricFootballResponse.ResponseType.OPPONENT_PLAYERS
                        : GeometricFootballResponse.ResponseType.TEAM_PLAYERS)
                .setTeamPlayers(teamPlayers.build())
                .build();
    }

    private GeometricFootballResponse.TeamPlayerData createTeamPlayer(final Game game, final GeometricFootballResponse.TeamPlayerType teamPlayerType) {
        return GeometricFootballResponse.TeamPlayerData.newBuilder()
                .setTeamPlayerType(teamPlayerType)
                .setName(game.getOwnerName())
                .setColor(game.getGrantedColor())
                .build();
    }

    private GeometricFootballResponse.TeamPlayerData createTeamPlayer(final Invitation invitation, final GeometricFootballResponse.TeamPlayerType teamPlayerType) {
        return GeometricFootballResponse.TeamPlayerData.newBuilder()
                .setTeamPlayerType(teamPlayerType)
                .setName(invitation.getInvitedPlayer())
                .setColor(invitation.getPreferredColor())
                .build();
    }
}
