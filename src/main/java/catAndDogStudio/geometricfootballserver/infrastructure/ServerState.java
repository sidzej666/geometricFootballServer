package catAndDogStudio.geometricfootballserver.infrastructure;

import io.netty.channel.ChannelId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServerState {
    @Getter
    private final Map<ChannelId, Game> games = new HashMap<>();

    private final Map<SelectableChannel, Game> hostedGames = new HashMap<>();
    private final Map<SelectableChannel, Game> waitingForGames = new HashMap<>();
    private final Map<SelectableChannel, Game> teamsWaitingForOpponents = new HashMap<>();
    private final Map<SelectableChannel, Game> playersInGame = new HashMap<>();

    public Map<SelectableChannel, Game> getHostedGames() {
        return hostedGames;
    }

    public Map<SelectableChannel, Game> getWaitingForGames() {
        return waitingForGames;
    }
    public Map<SelectableChannel, Game> getPlayersInGame() {
        return playersInGame;
    }
    public Map<SelectableChannel, Game> getTeamsWaitingForOpponents() {
        return teamsWaitingForOpponents;
    }
    public SelectableChannel getWaitingPlayer(final String playerName) {
        return waitingForGames.keySet().stream()
                .filter(k -> waitingForGames.get(k).getOwnerName().equals(playerName))
                .findFirst()
                .orElse(null);
    }
    public SelectableChannel getHostPlayer(final String hostName) {
        return hostedGames.keySet().stream()
                .filter(k -> hostedGames.get(k).getOwnerName().equals(hostName))
                .findFirst()
                .orElse(null);
    }

    public void addGame(final ChannelId channelId, final Game game) {
        games.put(channelId, game);
    }
    public void removeGame(final ChannelId channelId) {
        games.remove(channelId);
    }
}
