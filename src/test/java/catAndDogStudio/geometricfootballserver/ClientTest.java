package catAndDogStudio.geometricfootballserver;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
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
}
