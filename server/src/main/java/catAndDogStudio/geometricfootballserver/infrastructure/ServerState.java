package catAndDogStudio.geometricfootballserver.infrastructure;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.channels.SelectableChannel;
import java.util.Comparator;
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
    private final ChannelGroup waitingForOpponents;
    private final ChannelGroup activeGames;

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
    public Optional<Game> findOpponent(final ChannelId myChannelId) {
        return waitingForOpponents.stream()
                .filter(c -> !c.id().equals(myChannelId))
                .map(c -> games.get(c.id()))
                .min(Comparator.comparing(oc -> oc.getReadyForGameTime()));
    }
    public void moveFromWaitingForGamesToPlayersInGame(Channel channel) {
        waitingForGames.remove(channel);
        playersInGames.add(channel);
    }

    public void moveFromPlayersInGamesToWaitingForGame(Channel channel) {
        playersInGames.remove(channel);
        waitingForGames.add(channel);
    }

    public void moveToWaitingForGames(Channel channel) {
        hosts.remove(channel);
        waitingForGames.add(channel);
    }

    public void moveToHosts(Channel channel) {
        waitingForGames.remove(channel);
        hosts.add(channel);
    }

    public void moveFromHostToWaitingForOpponents(Channel channel) {
        hosts.remove(channel);
        waitingForOpponents.add(channel);
    }

    public void moveFromWaitingForOpponentsToHosts(Channel channel) {
        waitingForOpponents.remove(channel);
        hosts.add(channel);
    }

    public void moveFromWaitingForOpponentsToActiveGames(Channel channel) {
        waitingForOpponents.remove(channel);
        activeGames.add(channel);
    }

    public void moveFromActiveGameToWaitingForOpponents(Channel channel) {
        activeGames.remove(channel);
        waitingForOpponents.add(channel);
    }
}
