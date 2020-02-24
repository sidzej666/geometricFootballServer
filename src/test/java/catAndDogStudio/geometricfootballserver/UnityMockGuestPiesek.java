package catAndDogStudio.geometricfootballserver;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import org.junit.Test;

public class UnityMockGuestPiesek extends UnityMocksBase {

    @Test
    public void reset() throws Exception {
        resetClients();
    }
    // piesek - GUEST
    @Test
    public void awaitGameByPiesek() throws Exception {
        proxyServerClient.writeAndRead(0, mockFactory.authenticate("piesek", "1"));
        proxyServerClient.writeAndRead(0, mockFactory.awaitGame("piesek", "waiting for game", "RED"));
    }
    @Test
    public void acceptHostMysioInvitationByGuestPiesek() throws Exception {
        proxyServerClient.writeAndRead(0, mockFactory.invitationResult("mysio", "piesek",
                GeometricFootballRequest.TeamInvitationDirection.FROM_PLAYER_TO_HOST, GeometricFootballRequest.TeamInvitationAction.ACCEPT));
        proxyServerClient.read(0);
    }
    @Test
    public void rejectHostMysioInvitationByGuestPiesek() throws Exception {
        proxyServerClient.writeAndRead(0, mockFactory.invitationResult("mysio", "piesek",
                GeometricFootballRequest.TeamInvitationDirection.FROM_PLAYER_TO_HOST, GeometricFootballRequest.TeamInvitationAction.REJECT));
    }
    @Test
    public void joinGameByPiesekToHostMysio() throws Exception {
        awaitGameByPiesek();
        sendJoinGameByPiesekToHostMysio();
    }
    @Test
    public void sendJoinGameByPiesekToHostMysio() throws Exception {
        proxyServerClient.writeAndRead(0, mockFactory.sendGuestInvitationToHost("mysio", "piesek", "RED"));
    }
    @Test
    public void rejectJoinGameInvitationByPiesekToHostMysio() throws Exception {
        proxyServerClient.writeAndRead(0, mockFactory.invitationResult("mysio", "piesek",
                GeometricFootballRequest.TeamInvitationDirection.FROM_PLAYER_TO_HOST, GeometricFootballRequest.TeamInvitationAction.REJECT));
    }
}
