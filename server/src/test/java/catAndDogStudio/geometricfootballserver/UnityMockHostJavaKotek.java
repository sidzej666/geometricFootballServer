package catAndDogStudio.geometricfootballserver;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import org.junit.Before;
import org.junit.Test;

public class UnityMockHostJavaKotek extends UnityMocksBase {
    @Test
    public void reset() throws Exception {
        resetClients();
    }

    @Before
    public void clearPendingMessages() {
        proxyServerClient.clearPendingMessage(1);
    }

    // jakaKotek - HOST
    @Test
    public void hostGameByJavaKotek() {
        proxyServerClient.writeAndRead(1, mockFactory.authenticate("javaKotek", "0987654321"));
        proxyServerClient.writeAndRead(1, mockFactory.hostGame("javaKotek", "javaKotek game", "RED"));
        //setTeamDataByHostMysioToGuestPiesek();
    }
    @Test
    public void inviteAsHostJavaKotekGuestMysio() {
        proxyServerClient.writeAndRead(1, mockFactory.sendHostInvitationToGuest("javaKotek", "mysio", "BLACK"));
    }
    @Test
    public void hostGameByJavaKotekAndInviteGuestMysio() {
        hostGameByJavaKotek();
        inviteAsHostJavaKotekGuestMysio();
    }
    @Test
    public void rejectGuestMysioInvitationByHostJavaKotek() {
        proxyServerClient.writeAndRead(1, mockFactory.invitationResult("javaKotek", "mysio",
                GeometricFootballRequest.TeamInvitationDirection.FROM_HOST_TO_PLAYER, GeometricFootballRequest.TeamInvitationAction.REJECT));
    }
    @Test
    public void acceptGuestMysioInvitationByHostJavaKotek() {
        proxyServerClient.writeAndRead(1, mockFactory.invitationResult("javaKotek", "mysio",
                GeometricFootballRequest.TeamInvitationDirection.FROM_HOST_TO_PLAYER, GeometricFootballRequest.TeamInvitationAction.ACCEPT));
    }
    @Test
    public void kickGuestMysioByHostJavaKotek() {
        proxyServerClient.writeAndRead(1, mockFactory.kickPlayer("mysio"));
        proxyServerClient.read(1);
    }
    @Test
    public void readFromJavaKotekClient() {
        proxyServerClient.read(1);
    }
    @Test
    public void leaveGame() {
        proxyServerClient.write(1, mockFactory.leaveTeam());
    }
    @Test
    public void disconnect() {
        proxyServerClient.resetClient(1);
    }
    @Test
    public void setTeamHexagoniaFlyiers() {
        proxyServerClient.writeAndRead(1, mockTeamFactory.hexagoniaFlyers());
        proxyServerClient.writeAndRead(1, mockTeamFactory.hexagoniaFlyersTactic());
        proxyServerClient.writeAndRead(1, mockTeamFactory.hexagoniaFlyersPlayers());
        proxyServerClient.writeAndRead(1, mockTeamFactory.hexagoniaFlyersTacticMapping());
        proxyServerClient.writeAndRead(1, mockTeamFactory.hexagoniaFlyersPlayerFootballerMapping("javaKotek"));
    }
    @Test
    public void setFootballerMappingsSubsetToMysio() {
        proxyServerClient.writeAndRead(1, mockTeamFactory.hexagoniaFlyersPlayerFootballerMappingSubset("mysio"));
    }
    @Test
    public void setFootballerMappingsSubsetToJavaKotek() {
        proxyServerClient.writeAndRead(1, mockTeamFactory.hexagoniaFlyersPlayerFootballerMappingSubset("javaKotek"));
    }
    @Test
    public void readyForGame() {
        proxyServerClient.writeAndRead(1, mockFactory.readyForGame());
    }
    @Test
    public void goBackToHostingGame() {
        proxyServerClient.writeAndRead(1, mockFactory.goBackToHostingGame());
    }
    @Test
    public void loginAndReadyForGame() {
        hostGameByJavaKotek();
        setTeamHexagoniaFlyiers();
        readyForGame();
    }
}
