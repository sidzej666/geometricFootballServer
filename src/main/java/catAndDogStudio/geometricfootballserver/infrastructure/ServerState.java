package catAndDogStudio.geometricfootballserver.infrastructure;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServerState {
    private final ChannelGroup waitingForGames;
    private final ChannelGroup hosts;
    private final ChannelGroup playersInGames;

    @Getter
    private final Map<ChannelId, Game> games = new HashMap<>();

    private final Map<SelectableChannel, Game> hostedGames = new HashMap<>();
    private final Map<SelectableChannel, Game> waitingForGamesOld = new HashMap<>();
    private final Map<SelectableChannel, Game> teamsWaitingForOpponents = new HashMap<>();
    private final Map<SelectableChannel, Game> playersInGame = new HashMap<>();

    public Map<SelectableChannel, Game> getHostedGames() {
        return hostedGames;
    }

    public Map<SelectableChannel, Game> getWaitingForGamesOld() {
        return waitingForGamesOld;
    }
    public Map<SelectableChannel, Game> getPlayersInGame() {
        return playersInGame;
    }
    public Map<SelectableChannel, Game> getTeamsWaitingForOpponents() {
        return teamsWaitingForOpponents;
    }
    public SelectableChannel getWaitingPlayer(final String playerName) {
        return waitingForGamesOld.keySet().stream()
                .filter(k -> waitingForGamesOld.get(k).getOwnerName().equals(playerName))
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
    public Optional<Game> findWaitingPlayer(final String username) {
        return waitingForGames.stream()
                .map(c -> games.get(c.id()))
                .filter(g -> g.getOwnerName().equals(username))
                .findFirst();
    }
    public Optional<Game> findHostPlayer(final String username) {
        return hosts.stream()
                .map(c -> games.get(c.id()))
                .filter(g -> g.getOwnerName().equals(username))
                .findFirst();
    }
    public void moveFromWaitingForGamesToPlayersInGame(Channel channel) {
        waitingForGames.remove(channel);
        playersInGames.add(channel);
    }

    public void moveFromPlayersInGamesToWaitingForGame(Channel channel) {
        playersInGames.remove(channel);
        waitingForGames.add(channel);
    }
}
