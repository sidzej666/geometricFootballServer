package catAndDogStudio.geometricfootballserver;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import org.junit.Test;

public class UnickMockHostJavaKotek extends UnityMocksBase {
    @Test
    public void reset() throws Exception {
        resetClients();
    }

    // jakaKotek - HOST
    @Test
    public void hostGameByJavaKotek() throws Exception {
        proxyServerClient.writeAndRead(1, mockFactory.authenticate("javaKotek", "0987654321"));
        proxyServerClient.writeAndRead(1, mockFactory.hostGame("javaKotek", "javaKotek game", "GREEN"));
        //setTeamDataByHostMysioToGuestPiesek();
    }
    @Test
    public void inviteAsHostJavaKotekGuestMysio() throws Exception {
        proxyServerClient.writeAndRead(1, mockFactory.sendHostInvitationToGuest("javaKotek", "mysio", "RED"));
    }
    @Test
    public void hostGameByJavaKotekAndInviteGuestMysio() throws Exception {
        hostGameByJavaKotek();
        inviteAsHostJavaKotekGuestMysio();
    }
    @Test
    public void rejectGuestMysioInvitationByHostJavaKotek() throws Exception {
        proxyServerClient.writeAndRead(1, mockFactory.invitationResult("javaKotek", "mysio",
                GeometricFootballRequest.TeamInvitationDirection.FROM_HOST_TO_PLAYER, GeometricFootballRequest.TeamInvitationAction.REJECT));
    }
    @Test
    public void acceptGuestMysioInvitationByHostJavaKotek() throws Exception {
        proxyServerClient.writeAndRead(1, mockFactory.invitationResult("javaKotek", "mysio",
                GeometricFootballRequest.TeamInvitationDirection.FROM_HOST_TO_PLAYER, GeometricFootballRequest.TeamInvitationAction.ACCEPT));
        proxyServerClient.read(1);
    }
    @Test
    public void kickGuestMysioByHostJavaKotek() throws Exception {
        proxyServerClient.writeAndRead(1, mockFactory.kickPlayer("mysio"));
        proxyServerClient.read(1);
    }
    @Test
    public void readFromJavaKotekClient() throws Exception {
        proxyServerClient.read(1);
    }
}
