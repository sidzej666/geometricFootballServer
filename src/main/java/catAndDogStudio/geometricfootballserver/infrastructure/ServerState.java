package catAndDogStudio.geometricfootballserver.infrastructure;

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
    private final Map<SelectableChannel, Game> hostedGames = new HashMap<>();
    private final Map<SelectableChannel, Game> waitingForGames = new HashMap<>();
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


}
