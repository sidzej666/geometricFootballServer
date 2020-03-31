package catAndDogStudio.geometricfootballserver.netty;

import catAndDogStudio.geometricfootballserver.TestConstants;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.mocks.MockFactory;
import catAndDogStudio.geometricfootballserver.netty.infrastructure.NettyTestClient;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import com.cat_and_dog_studio.geometric_football.protocol.Model;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@EnableAsync
@SpringBootTest
@ActiveProfiles("nettyTestServer")
public class ClientTest {

    @Autowired
    private TestConstants testConstants;
    @Autowired
    private ServerState serverState;
    @Autowired
    private MockFactory mockFactory;
    private NettyTestClient nettyTestClient;
    private NettyTestClient nettyTestClientTwo;

    @Before
    public void resetClient() throws Exception {
        nettyTestClient = new NettyTestClient(testConstants.getPortForTests());
        nettyTestClient.run();

        nettyTestClientTwo = new NettyTestClient(testConstants.getPortForTests());;
        nettyTestClientTwo.run();
    }

    @After
    public void closeClient() throws Exception {
        nettyTestClient.shutdown();
        nettyTestClientTwo.shutdown();
    }

    @Test
    public void authenticate() throws Exception {
        nettyTestClient.send(mockFactory.authenticate("javaKotek", "1234567890"));
        GeometricFootballResponse.Response response = nettyTestClient.receive();
        assertThat(response).isEqualTo(GeometricFootballResponse.Response.newBuilder()
            .setType(GeometricFootballResponse.ResponseType.AUTHENTICATION)
            .setAuthenticationResponse(GeometricFootballResponse.AuthenticationResponse.newBuilder()
                    .setUsername("unknown")
                    .setMessage("bad credentials")
                    .build())
            .build());

        nettyTestClient.send(mockFactory.authenticate("javaKotek", "0987654321"));
        response = nettyTestClient.receive();
        assertThat(response).isEqualTo(GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.AUTHENTICATION)
                .setAuthenticationResponse(GeometricFootballResponse.AuthenticationResponse.newBuilder()
                        .setUsername("javaKotek")
                        .setMessage("Hello, javaKotek!")
                        .build())
                .build());
    }

    @Test
    public void doubleDisconnect() throws Exception {
        nettyTestClient.send(mockFactory.authenticate("javaKotek", "1234567890"));
        nettyTestClient.send(mockFactory.disconnect());

        nettyTestClient = new NettyTestClient(testConstants.getPortForTests());
        nettyTestClient.run();
        nettyTestClient.send(mockFactory.disconnect());
    }

    @Test
    public void shouldHostGameAndDisconnect() throws Exception {
        nettyTestClient.send(mockFactory.authenticate("javaKotek", "0987654321"));
        nettyTestClient.receive();
        nettyTestClient.send(mockFactory.hostGame("javaKotek", "Gra java-kotka","green"));
        final GeometricFootballResponse.Response hostGameResponse = nettyTestClient.receive();
        assertThat(hostGameResponse).isEqualTo(GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.GAME_HOSTED)
                .setHostGameResponse(GeometricFootballResponse.HostGameResponse.newBuilder()
                        .setUsername("javaKotek")
                        .setGameName("Gra java-kotka")
                        .setHostColor("green")
                        .build())
                .build()
        );
        nettyTestClient.send(mockFactory.disconnect());
        final GeometricFootballResponse.Response disconnectRepsonse = nettyTestClient.receive();
        assertThat(disconnectRepsonse).isEqualTo(GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.DISCONNECT)
                .setDisconnectResponse(GeometricFootballResponse.DisconnectResponse.newBuilder()
                        .setMessage("Bye")
                        .build())
                .build()
        );
    }

    @Test
    public void shouldAwaitGame() throws Exception {
        nettyTestClient.send(mockFactory.awaitGame("javaKotek", "tylko powazne oferty",
                "różowy"));
        final GeometricFootballResponse.Response wrongGameState = nettyTestClient.receive();
        assertThat(wrongGameState).isEqualTo(GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.ERROR)
                .setErrorResponse(GeometricFootballResponse.ErrorResponse.newBuilder()
                        .setMessage("Bad state for operation AWAIT_GAME, current state: AWAITING_AUTHENTICATION")
                        .build())
                .build()
        );
        nettyTestClient.send(mockFactory.authenticate("javaKotek", "0987654321"));
        nettyTestClient.receive();
        nettyTestClient.send(mockFactory.awaitGame("javaKotek", "tylko powazne oferty",
                "różowy"));
        final GeometricFootballResponse.Response awaitingGameResposne = nettyTestClient.receive();
        assertThat(awaitingGameResposne).isEqualTo(GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.AWAITING_FOR_GAME)
                .setAwaitGameResponse(GeometricFootballResponse.AwaitGameResponse.newBuilder()
                        .setUsername("javaKotek")
                        .setWaitingMessage("tylko powazne oferty")
                        .setPreferredColor("różowy")
                        .build())
                .build()
        );
    }

    @Test
    public void shouldGetPlayers() throws Exception {
        nettyTestClient.send(mockFactory.authenticate("javaKotek", "0987654321"));
        nettyTestClient.receive();
        nettyTestClient.send(mockFactory.awaitGame("javaKotek", "tylko powazne oferty",
                "różowy"));
        nettyTestClient.receive();

        nettyTestClientTwo.send(mockFactory.authenticate("kotekMaskotek", "1234567890"));
        nettyTestClientTwo.receive();
        nettyTestClientTwo.send(mockFactory.hostGame("kotekMaskotek", "Gra java-kotka","green"));
        nettyTestClientTwo.receive();

        nettyTestClient.send(mockFactory.getPlayers(Model.GetPlayersMode.GAME_HOSTS));
        final GeometricFootballResponse.Response hostsResponse = nettyTestClient.receive();
        assertThat(hostsResponse).isEqualTo(GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.GET_PLAYERS)
                .setGetPlayers(GeometricFootballResponse.GetPlayersResponse.newBuilder()
                        .setMode(Model.GetPlayersMode.GAME_HOSTS)
                        .addPlayerData(0, GeometricFootballResponse.PlayerData.newBuilder()
                                .setColor("green")
                                .setName("kotekMaskotek")
                                .build())
                        .build())
                .build());

        nettyTestClientTwo.send(mockFactory.getPlayers(Model.GetPlayersMode.WAITING_FOR_GAMES));
        final GeometricFootballResponse.Response waitingForGamesResponse = nettyTestClientTwo.receive();
        assertThat(waitingForGamesResponse).isEqualTo(GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.GET_PLAYERS)
                .setGetPlayers(GeometricFootballResponse.GetPlayersResponse.newBuilder()
                        .setMode(Model.GetPlayersMode.WAITING_FOR_GAMES)
                        .addPlayerData(0, GeometricFootballResponse.PlayerData.newBuilder()
                                .setColor("różowy")
                                .setName("javaKotek")
                                .build())
                        .build())
                .build());
    }
}
