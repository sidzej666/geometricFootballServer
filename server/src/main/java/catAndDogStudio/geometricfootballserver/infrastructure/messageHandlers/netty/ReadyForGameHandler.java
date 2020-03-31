package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.InvitationService;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.ReadyForGameService;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import com.cat_and_dog_studio.geometric_football.protocol.Model;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadyForGameHandler extends BaseMessageHandler {
    private final ServerState serverState;
    private final Set<PlayerState> allowedStates = EnumSet.of(PlayerState.GAME_HOST, PlayerState.WAIT_FOR_OPPONENTS);
    private final InvitationService invitationService;
    private final ReadyForGameService readyForGameService;

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final boolean isReadyForGame = request.getReadyForGame().getReady();
        if (PlayerState.WAIT_FOR_OPPONENTS.equals(game.getPlayerState()) && isReadyForGame
            || PlayerState.GAME_HOST.equals(game.getPlayerState()) && !isReadyForGame) {
            sendMessage(channel, game, error(String.format("Bad state % for readyForGame with % parameter", game.getPlayerState(), isReadyForGame)));
            return;
        }
        if (isReadyForGame) {
            final Optional<String> validationError = validateGame(game);
            if (validationError.isPresent()) {
                sendMessage(channel, game, error(validationError.get()));
                return;
            }
            game.getInvitations().forEach(i -> invitationService.cancelInvitation(i, game, false));
            waitForOpponents(game);
            serverState.moveFromHostToWaitingForOpponents(game.getChannel());
            readyForGameService.ifPossibleFindOpponentAndTransitionToInGame(game);
        } else {
            goBackToHosting(game);
            serverState.moveFromWaitingForOpponentsToHosts(game.getChannel());
        }
    }

    private void waitForOpponents(final Game game) {
        game.setPlayerState(PlayerState.WAIT_FOR_OPPONENTS);
        game.setReadyForGameTime((new Date()).getTime());
        GeometricFootballResponse.Response readyForGame = createReadyForGameRespone();
        sendMessage(game.getChannel(), game, readyForGame);
        game.getPlayersInTeam().values()
                .forEach(guest -> sendMessage(guest.getChannel(), guest, readyForGame));
    }

    private void goBackToHosting(final Game game) {
        game.setPlayerState(PlayerState.GAME_HOST);
        GeometricFootballResponse.Response goBackToHosting = createGoBackToHostingRespone();
        sendMessage(game.getChannel(), game, goBackToHosting);
        game.getPlayersInTeam().values()
                .forEach(guest -> sendMessage(guest.getChannel(), guest, goBackToHosting));
    }

    private GeometricFootballResponse.Response createReadyForGameRespone() {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.READY_FOR_GAME)
                .setReadyForGameResponse(Model.ReadyForGame.newBuilder()
                        .setReady(true)
                        .build())
                .build();
    }

    private GeometricFootballResponse.Response createGoBackToHostingRespone() {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.READY_FOR_GAME)
                .setReadyForGameResponse(Model.ReadyForGame.newBuilder()
                        .setReady(false)
                        .build())
                .build();
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return allowedStates;
    }

    private Optional<String> validateGame(Game game) {
        if (game.getTeam() == null) {
            return Optional.of("team not set");
        }
        if (game.getPlayers() == null) {
            return Optional.of("players not set");
        }
        if (game.getPlayerFootballerMappings().isEmpty()) {
            return Optional.of("players-users mapping not set");
        }
        if (game.getTactic() == null) {
            return Optional.of("tactic not set");
        }
        if (game.getTacticMapping() == null) {
            return Optional.of("tactic mapping not set");
        }
        final Optional<String> mappingValidation = game.getPlayers().getPlayersList().stream()
                .filter(p -> !game.getPlayerFootballerMappings().keySet().contains(p.getUniqueId()))
                .findAny()
                .map(p -> Optional.of(String.format("Player %s not found in players-users mappings", p.getUniqueId())))
                .orElse(Optional.empty());
        if (mappingValidation.isPresent()) {
            return mappingValidation;
        }
        return Optional.empty();
    }
}
