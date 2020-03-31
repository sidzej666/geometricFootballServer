package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageCreators;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty.BaseMessageHandler;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import com.cat_and_dog_studio.geometric_football.protocol.Model;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamInfoMessageCreator extends BaseMessageHandler {
    private final PlayersInTeamMessageCreator playersInTeamMessageCreator;
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

    public void sendAllAvailableTeamInfo(final Game target, final Game gameToFetchInfo, final boolean opponent,
                                         final boolean withInvitations) {
        final Channel channel = target.getChannel();
        sendMessage(channel, target, playersInTeamMessageCreator.createTeamInfo(gameToFetchInfo, opponent, withInvitations));
        if (gameToFetchInfo.getTeam() != null) {
            sendMessage(channel, target, teamResponse(gameToFetchInfo.getTeam(), opponent));
        }
        if (gameToFetchInfo.getPlayers() != null) {
            sendMessage(channel, target, teamPlayersResponse(gameToFetchInfo.getPlayers(), opponent));
        }
        if (gameToFetchInfo.getTactic() != null) {
            sendMessage(channel, target, tacticResponse(gameToFetchInfo.getTactic(), opponent));
        }
        if (gameToFetchInfo.getTacticMapping() != null) {
            sendMessage(channel, target, tacticMappingResponse(gameToFetchInfo.getTacticMapping(), opponent));
        }
        if (!gameToFetchInfo.getPlayerFootballerMappings().isEmpty()) {
            sendMessage(channel, target, sendPlayerFootballeResponse(gameToFetchInfo.getPlayerFootballerMappings(), opponent));
        }
    }

    private GeometricFootballResponse.Response teamResponse(final Model.Team team, final boolean opponent) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(opponent ? GeometricFootballResponse.ResponseType.SET_OPPONENT_TEAM
                        : GeometricFootballResponse.ResponseType.SET_TEAM)
                .setTeam(team)
                .build();
    }

    private GeometricFootballResponse.Response teamPlayersResponse(final Model.Players players, final boolean opponent) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(opponent ? GeometricFootballResponse.ResponseType.SET_OPPONENT_TEAM_PLAYERS
                        : GeometricFootballResponse.ResponseType.SET_TEAM_PLAYERS)
                .setPlayers(players)
                .build();
    }

    private GeometricFootballResponse.Response sendPlayerFootballeResponse(
            final Map<String, Model.PlayerOwner> playerFootballerMappings, final boolean opponent) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(opponent ? GeometricFootballResponse.ResponseType.SET_OPPONENT_TEAM_PLAYERS_USERS_MAPPING
                        : GeometricFootballResponse.ResponseType.SET_TEAM_PLAYERS_USERS_MAPPING)
                .setPlayerFootballerMappings(Model.PlayerFootballerMappings.newBuilder()
                        .addAllPlayerOwners(playerFootballerMappings.values())
                        .build())
                .build();
    }

    private GeometricFootballResponse.Response tacticResponse(final Model.Tactic tactic, final boolean opponent) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(opponent ? GeometricFootballResponse.ResponseType.SET_OPPONENT_TACTIC
                        : GeometricFootballResponse.ResponseType.SET_TACTIC)
                .setTactic(tactic)
                .build();
    }

    private GeometricFootballResponse.Response tacticMappingResponse(final Model.TacticMapping tacticMapping,
                                                                     final boolean opponent) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(opponent ? GeometricFootballResponse.ResponseType.SET_OPPONENT_TACTIC_MAPPING
                        : GeometricFootballResponse.ResponseType.SET_TACTIC_MAPPING)
                .setTacticMapping(tacticMapping)
                .build();
    }
}
