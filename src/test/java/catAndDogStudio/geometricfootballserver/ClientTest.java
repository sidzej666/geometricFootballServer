package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.OutputMessages;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.net.SocketFactory;
import java.net.Socket;

import static catAndDogStudio.geometricfootballserver.infrastructure.MessagesUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@SpringBootTest
@ActiveProfiles("testServer")
public class ClientTest {

    @Autowired
    private TestConstants testConstants;
    @Autowired
    private ServerState serverState;

    @Before
    public void resetServer() {
        serverState.getWaitingForGamesOld().clear();
        serverState.getPlayersInGame().clear();
        serverState.getHostedGames().clear();
        serverState.getTeamsWaitingForOpponents().clear();
    }

    @Test
    public void awaitGame() throws Exception {
        Socket socket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        String mauResponse = writeAndRead(socket, ClientMessages.authenticate("0987654321"))[0];
        String awaitGameResponse = writeAndRead(socket, ClientMessages.awaitGame("javaKotek", "RED"))[0];
        assertThat(mauResponse).isEqualTo("VERY_CUTE_MAU_HELLO_KITTY;javaKotek");
        assertThat(awaitGameResponse).isEqualTo("AWAITING_FOR_GAME;javaKotek");
    }

    @Test
    public void shouldJoinTeamWithGuestInvitation() throws Exception {
        Socket hostSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        String hostMauResponse = writeAndRead(hostSocket, ClientMessages.authenticate("0987654321"))[0];

        Socket guestSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        String guestMauResponse = writeAndRead(guestSocket, ClientMessages.authenticate("1"))[0];

        String hostGameResponse = writeAndRead(hostSocket, ClientMessages.hostGame("javaKotek", "RED"))[0];
        String awaitGameResponse = writeAndRead(guestSocket, ClientMessages.awaitGame("piesek", "RED"))[0];

        String invitationToHostResponse = writeAndRead(guestSocket, ClientMessages.joinGameRequest("javaKotek"))[0];
        assertThat(invitationToHostResponse).isEqualTo(ClientResponses.joinRequestSentToHost("javaKotek"));
        String invitationReceivedByHost = read(hostSocket)[0];
        assertThat(invitationReceivedByHost).isEqualTo(ClientResponses.joinRequestReceivedByHost("piesek", "RED"));

        String invitationNotFound = writeAndRead(hostSocket, ClientMessages.acceptGuestInvitation("piesek2", "BLUE"))[0];
        assertThat(invitationNotFound).isEqualTo(ClientResponses.invitationNotFound("piesek2"));
        String invitationAccepted = writeAndRead(hostSocket, ClientMessages.acceptGuestInvitation("piesek", "BLUE"))[0];
        assertThat(invitationAccepted).isEqualTo(ClientResponses.kittyJoinedGame("piesek"));
        String kittyJoinedGameRespones[] = read(guestSocket);
        assertThat(kittyJoinedGameRespones[0]).isEqualTo(ClientResponses.kittyJoinedGame("piesek"));
        assertThat(kittyJoinedGameRespones[1]).isEqualTo(ClientResponses.playersList("javaKotek", "RED", "piesek", "BLUE"));

        write(hostSocket, ClientMessages.setPlayerMappings("1", "piesek", "2", "javaKotek"));
        String playersMappingSendToGuest = read(guestSocket)[0];
        assertThat(playersMappingSendToGuest).isEqualTo(ClientResponses.playerMappings("1", "piesek", "2", "javaKotek"));
    }

    @Test
    public void noSuchHostingKitty() throws Exception {
        Socket guestSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        String guestMauResponse = writeAndRead(guestSocket, ClientMessages.authenticate("1"))[0];
        String awaitGameResponse = writeAndRead(guestSocket, ClientMessages.awaitGame("piesek", "RED"))[0];
        String invitationToHostResponse = writeAndRead(guestSocket, ClientMessages.joinGameRequest("javaKotek"))[0];
        assertThat(invitationToHostResponse).isEqualTo(ClientResponses.noSuchHostingKitty("javaKotek"));
    }

    @Test
    public void shouldNotAllowInvitationsToWaitingForOponentsHost() throws Exception {
        Socket hostSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        writeAndRead(hostSocket, ClientMessages.authenticate("0987654321"));

        Socket guestSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        writeAndRead(guestSocket, ClientMessages.authenticate("1"));

        writeAndRead(hostSocket, ClientMessages.hostGame("javaKotek", "RED"));
        writeAndRead(guestSocket, ClientMessages.awaitGame("piesek", "RED"));

        write(hostSocket, ClientMessages.setTeam());
        write(hostSocket, ClientMessages.setTeamPlayers());
        write(hostSocket, ClientMessages.setTeamTactic());
        write(hostSocket, ClientMessages.setTacticMapping());
        write(hostSocket, ClientMessages.setPlayerMappings("a8dab18d-572b-4a5a-91bd-6e75abda6234", "mysio",
                "c78a99da-899a-40d1-bad7-5816b41e8f07", "mysio", "8f3b10ed-2663-4946-8dbe-9553f83736ca", "mysio",
                "42cda9fb-e418-404b-96e1-455e999215d4", "mysio", "8d9e37fa-2e59-4281-a254-d9e00e1e3ee5", "mysio"));
        String readyForGameResponse = writeAndRead(hostSocket, ClientMessages.readyForGame())[0];
        assertThat(readyForGameResponse).isEqualTo(OutputMessages.getReadyForGameTransitionMessage());

        String invitationRejectedMessage = writeAndRead(guestSocket, ClientMessages.joinGameRequest("javaKotek"))[0];
        assertThat(invitationRejectedMessage).isEqualTo(OutputMessages.NO_SUCH_HOSTING_KITTY + Constants.MESSAGE_SEPARATOR
            + "javaKotek");
    }

    @Test
    public void readyForGameValidations() throws Exception {
        Socket hostSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        writeAndRead(hostSocket, ClientMessages.authenticate("0987654321"));

        Socket guestSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        writeAndRead(guestSocket, ClientMessages.authenticate("1"));

        writeAndRead(hostSocket, ClientMessages.hostGame("javaKotek", "RED"));
        writeAndRead(guestSocket, ClientMessages.awaitGame("piesek", "RED"));

        readyForGameResponseTest("team not set", hostSocket);
        write(hostSocket, ClientMessages.setTeam());
        readyForGameResponseTest("players not set", hostSocket);
        write(hostSocket, ClientMessages.setTeamPlayers());
        readyForGameResponseTest("players-users mapping not set", hostSocket);
        write(hostSocket, ClientMessages.setPlayerMappings("a8dab18d-572b-4a5a-91bd-6e75abda6234", "mysio",
                "c78a99da-899a-40d1-bad7-5816b41e8f07", "mysio", "8f3b10ed-2663-4946-8dbe-9553f83736ca", "mysio",
                "42cda9fb-e418-404b-96e1-455e999215d4", "mysio", "8d9e37fa-2e59-4281-a254-d9e00e1e3ee5", "mysio"));
        readyForGameResponseTest("tactic not set", hostSocket);
        write(hostSocket, ClientMessages.setTeamTactic());
        readyForGameResponseTest("tactic mapping not set", hostSocket);
        write(hostSocket, ClientMessages.setTacticMapping());
        String readyForGameResponse = writeAndRead(hostSocket, ClientMessages.readyForGame())[0];
        assertThat(readyForGameResponse).isEqualTo(OutputMessages.getReadyForGameTransitionMessage());
    }
    @Test
    public void shouldSendMessageToAllPlayersWhenTransitionBackToTeamCreation() throws Exception {
        Socket hostSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        String[] messages = writeAndRead(hostSocket, ClientMessages.authenticate("0987654321"));

        Socket guestSocket = SocketFactory.getDefault().createSocket("localhost", testConstants.getPortForTests());
        messages = writeAndRead(guestSocket, ClientMessages.authenticate("1"));

        messages = writeAndRead(hostSocket, ClientMessages.hostGame("javaKotek", "RED"));
        messages = writeAndRead(guestSocket, ClientMessages.awaitGame("piesek", "RED"));

        messages = writeAndRead(guestSocket, ClientMessages.joinGameRequest("javaKotek"));
        messages = read(hostSocket);
        messages = writeAndRead(hostSocket, ClientMessages.acceptGuestInvitation("piesek", "BLUE"));
        messages = read(guestSocket);

        write(hostSocket, ClientMessages.setTeam());
        read(guestSocket);
        write(hostSocket, ClientMessages.setTeamPlayers());
        read(guestSocket);
        write(hostSocket, ClientMessages.setPlayerMappings("a8dab18d-572b-4a5a-91bd-6e75abda6234", "mysio",
                "c78a99da-899a-40d1-bad7-5816b41e8f07", "mysio", "8f3b10ed-2663-4946-8dbe-9553f83736ca", "mysio",
                "42cda9fb-e418-404b-96e1-455e999215d4", "mysio", "8d9e37fa-2e59-4281-a254-d9e00e1e3ee5", "mysio"));
        read(guestSocket);
        write(hostSocket, ClientMessages.setTeamTactic());
        read(guestSocket);
        write(hostSocket, ClientMessages.setTacticMapping());
        read(guestSocket);
        messages = writeAndRead(hostSocket, ClientMessages.readyForGame());
        read(guestSocket);
        messages = writeAndRead(hostSocket, ClientMessages.goBackToTeamCreation());
        assertThat(messages[0]).isEqualTo(OutputMessages.getGoBackToTeamPreparationTransition());
        messages = read(guestSocket);
        assertThat(messages[0]).isEqualTo(OutputMessages.getGoBackToTeamPreparationTransition());
    }

    private void readyForGameResponseTest(String expectedMessage, Socket hostSocket) throws Exception {
        String readyForGameResponse = writeAndRead(hostSocket, ClientMessages.readyForGame())[0];
        assertThat(readyForGameResponse).isEqualTo(OutputMessages.READY_FOR_GAME_VALIDATION_ERROR + Constants.MESSAGE_SEPARATOR
                + expectedMessage);
    }
}
