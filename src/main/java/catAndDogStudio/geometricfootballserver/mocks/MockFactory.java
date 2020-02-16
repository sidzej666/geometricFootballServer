package catAndDogStudio.geometricfootballserver.mocks;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import org.springframework.stereotype.Service;

@Service
public class MockFactory {

    public GeometricFootballRequest.Request authenticate(final String username, final String password) {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.AUTHENTICATION)
                .setAuthentication(GeometricFootballRequest.Authentication.newBuilder()
                        .setUsername(username)
                        .setPassword(password)
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request disconnect() {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.DISCONNECT)
                .setDisconnect(GeometricFootballRequest.Disconnect.newBuilder()
                        .setMessage("Bye")
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request hostGame(final String username, final String gameName,
                                                     final String hostColor) {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.HOST_GAME)
                .setHostGame(GeometricFootballRequest.HostGame.newBuilder()
                        .setUsername(username)
                        .setGameName(gameName)
                        .setHostColor(hostColor)
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request awaitGame(final String username, final String waitingMessage,
                                                      final String preferredColor) {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.AWAIT_GAME)
                .setAwaitGame(GeometricFootballRequest.AwaitGame.newBuilder()
                        .setUsername(username)
                        .setWaitingMessage(waitingMessage)
                        .setPreferredColor(preferredColor)
                        .build())
                .build();
    }

    public GeometricFootballRequest.Request getPlayers(final GeometricFootballRequest.GetPlayersMode getPlayersMode) {
        return GeometricFootballRequest.Request.newBuilder()
                .setType(GeometricFootballRequest.RequestType.GET_PLAYERS)
                .setGetPlayers(GeometricFootballRequest.GetPlayers.newBuilder()
                        .setMode(getPlayersMode)
                        .build())
                .build();
    }
}
