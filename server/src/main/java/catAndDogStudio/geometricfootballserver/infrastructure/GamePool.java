package catAndDogStudio.geometricfootballserver.infrastructure;

import java.util.LinkedList;
import java.util.Queue;

public class GamePool {
    public Queue<Game> availableGames = new LinkedList<>();

    private void initialize(final long maxNoOfConnections) {
        for(long i = 0; i < maxNoOfConnections; i++) {
            availableGames.add(new Game());
        }
    }

    public GamePool(long maxNumberOfConnections) {
        initialize(maxNumberOfConnections);
    }
    public Game getGame() {
        if (availableGames.isEmpty()) {
            throw new RuntimeException("all game objects used");
        }
        return availableGames.poll();
    }
    public void returnGameObject(final Game game) {
        availableGames.add(game);
    }
}
