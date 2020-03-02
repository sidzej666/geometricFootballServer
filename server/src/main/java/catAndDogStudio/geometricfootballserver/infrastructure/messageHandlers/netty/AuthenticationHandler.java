package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationHandler extends BaseMessageHandler {

    @Override
    protected void messageAction(final Channel channel, final Game game,
                                 final GeometricFootballRequest.Request request) {
        final GeometricFootballRequest.Authentication authentication = request.getAuthentication();
        if (isOk(authentication, "kotekMaskotek", "1234567890")) {
            game.setOwnerName("kotekMaskotek");
        } else if (isOk(authentication, "javaKotek", "0987654321")) {
            game.setOwnerName("javaKotek");
        } else if (isOk(authentication, "piesek", "1")) {
            game.setOwnerName("piesek");
        } else if (isOk(authentication, "mysio", "2")) {
            game.setOwnerName("mysio");
        } else {
            sendMessage(channel, game, notAuthenticated());
            return;
        }
        game.setPlayerState(PlayerState.AUTHENTICATED);
        game.setLastHearthBeatTime(new Date().getTime());
        sendMessage(channel, game, authenticated(authentication.getUsername()));
    }

    @Override
    protected Set<PlayerState> getPossibleStates() {
        return PlayerState.AUTHENTICATED.possibleStatesForTransition;
    }

    private boolean isOk(final GeometricFootballRequest.Authentication authentication, final String username, final String password) {
        return username.equals(authentication.getUsername()) && password.equals(authentication.getPassword());
    }

    private GeometricFootballResponse.Response authenticated(final String username) {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.AUTHENTICATION)
                .setAuthenticationResponse(GeometricFootballResponse.AuthenticationResponse.newBuilder()
                        .setMessage("Hello, " + username + "!")
                        .setUsername(username)
                        .build())
                .build();
    }

    private GeometricFootballResponse.Response notAuthenticated() {
        return GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.AUTHENTICATION)
                .setAuthenticationResponse(GeometricFootballResponse.AuthenticationResponse.newBuilder()
                        .setMessage("bad credentials")
                        .setUsername("unknown")
                        .build())
                .build();
    }
}
