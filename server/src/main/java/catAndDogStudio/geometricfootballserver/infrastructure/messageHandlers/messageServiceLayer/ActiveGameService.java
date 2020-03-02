package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer;

import catAndDogStudio.geometricfootballserver.infrastructure.ActiveGame;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.model.JsonFootballer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveGameService {
    private final ServerState serverState;
    private final PlayerObjectFactory playerObjectFactory;

    public void createActiveGameAndSetUpGameObjects(Game game, Game opponent) {
        final ActiveGame activeGame = new ActiveGame();
        // search players and players mappings to set up footballers
        final List<JsonFootballer> jsonFootballerList = playerObjectFactory.construct(game.getPlayers());
        activeGame.getFootballers();
    }
}
