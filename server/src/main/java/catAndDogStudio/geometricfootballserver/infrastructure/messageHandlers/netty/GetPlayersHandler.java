package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import com.cat_and_dog_studio.geometric_football.protocol.Model;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetPlayersHandler extends BaseMessageHandler {

    private final ChannelGroup waitingForGames;
    private final ChannelGroup hosts;
    private final ServerState serverState;

    private final Set<PlayerState> allowedStates = Stream.of(PlayerState.GAME_HOST, PlayerState.AWAITS_GAME,
            PlayerState.IN_GAME)
            .collect(Collectors.toSet());

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final GeometricFootballRequest.GetPlayers getPlayers = request.getGetPlayers();
        final GeometricFootballResponse.Response response;
        if (getPlayers.getMode() == Model.GetPlayersMode.GAME_HOSTS) {
            response = players(Model.GetPlayersMode.GAME_HOSTS, hosts);
        } else if (getPlayers.getMode() == Model.GetPlayersMode.WAITING_FOR_GAMES) {
            response = players(Model.GetPlayersMode.WAITING_FOR_GAMES, waitingForGames);
        } else {
            return;
        }

        sendMessage(channel, game, response);
    }

    private GeometricFootballResponse.Response players(final Model.GetPlayersMode getPlayersMode,
                                                       final ChannelGroup channelGroup) {
        final List<GeometricFootballResponse.PlayerData> playerData = channelGroup.stream()
                .map(channel -> channel.id())
                .map(channelId -> serverState.getGames().get(channelId))
                .map(game -> GeometricFootballResponse.PlayerData.newBuilder()
                        .setName(game.getOwnerName())
                        .setColor(getColor(game, getPlayersMode))
                        .build())
                .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                .collect(Collectors.toList());
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.GET_PLAYERS)
                .setGetPlayers(buildGetPlayers(playerData, getPlayersMode))
                .build();
    }

    private GeometricFootballResponse.GetPlayersResponse buildGetPlayers(final List<GeometricFootballResponse.PlayerData> playerData,
                                                                 final Model.GetPlayersMode getPlayersMode) {
        final GeometricFootballResponse.GetPlayersResponse.Builder builder = GeometricFootballResponse.GetPlayersResponse.newBuilder()
                .setMode(getPlayersMode);
        for(int i = 0; i < playerData.size(); i++) {
            builder.addPlayerData(i, playerData.get(i));
        }
        return builder.build();
    }

    private String getColor(final Game game, final Model.GetPlayersMode getPlayersMode) {
        if (getPlayersMode == Model.GetPlayersMode.GAME_HOSTS) {
            return game.getGrantedColor();
        } else if (getPlayersMode == Model.GetPlayersMode.WAITING_FOR_GAMES) {
            return game.getPreferredColor();
        }
        return game.getPreferredColor();
    }
}
