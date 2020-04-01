package catAndDogStudio.geometricfootballserver;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import org.junit.Before;
import org.junit.Test;

public class UnityMockHostKotekMaskotek extends UnityMocksBase {
    @Test
    public void reset() throws Exception {
        resetClients();
    }

    @Before
    public void clearPendingMessages() {
        proxyServerClient.clearPendingMessage(2);
    }

    // jakaKotek - HOST
    @Test
    public void hostGameByKotekMaskotek() {
        proxyServerClient.writeAndRead(2, mockFactory.authenticate("kotekMaskotek", "1234567890"));
        proxyServerClient.writeAndRead(2, mockFactory.hostGame("kotekMaskotek", "kotekMaskotek game", "RED"));
        //setTeamDataByHostMysioToGuestPiesek();
    }
    @Test
    public void inviteAsHostKotekMaskotekGuestMysio() {
        proxyServerClient.writeAndRead(2, mockFactory.sendHostInvitationToGuest("kotekMaskotek", "mysio", "BLACK"));
    }
    @Test
    public void hostGameByJavaKotekAndInviteGuestMysio() {
        hostGameByKotekMaskotek();
        inviteAsHostKotekMaskotekGuestMysio();
    }
    @Test
    public void rejectGuestMysioInvitationByHostKotekMaskotek() {
        proxyServerClient.writeAndRead(2, mockFactory.invitationResult("kotekMaskotek", "mysio",
                GeometricFootballRequest.TeamInvitationDirection.FROM_HOST_TO_PLAYER, GeometricFootballRequest.TeamInvitationAction.REJECT));
    }
    @Test
    public void acceptGuestMysioInvitationByKotekMaskotek() {
        proxyServerClient.writeAndRead(2, mockFactory.invitationResult("kotekMaskotek", "mysio",
                GeometricFootballRequest.TeamInvitationDirection.FROM_HOST_TO_PLAYER, GeometricFootballRequest.TeamInvitationAction.ACCEPT));
    }
    @Test
    public void kickGuestMysioByHostKotekMaskotek() {
        proxyServerClient.writeAndRead(2, mockFactory.kickPlayer("mysio"));
        proxyServerClient.read(2);
    }
    @Test
    public void leaveGame() {
        proxyServerClient.write(2, mockFactory.leaveTeam());
    }
    @Test
    public void disconnect() {
        proxyServerClient.resetClient(2);
    }
    @Test
    public void setTeamHexagoniaFlyiers() {
        proxyServerClient.writeAndRead(2, mockTeamFactory.hexagoniaFlyers());
        proxyServerClient.writeAndRead(2, mockTeamFactory.hexagoniaFlyersTactic());
        proxyServerClient.writeAndRead(2, mockTeamFactory.hexagoniaFlyersPlayers());
        proxyServerClient.writeAndRead(2, mockTeamFactory.hexagoniaFlyersTacticMapping());
        proxyServerClient.writeAndRead(2, mockTeamFactory.hexagoniaFlyersPlayerFootballerMapping("kotekMaskotek"));
    }
    @Test
    public void setFootballerMappingsSubsetToMysio() {
        proxyServerClient.writeAndRead(2, mockTeamFactory.hexagoniaFlyersPlayerFootballerMappingSubset("mysio"));
    }
    @Test
    public void setFootballerMappingsSubsetToKotekMaskotek() {
        proxyServerClient.writeAndRead(2, mockTeamFactory.hexagoniaFlyersPlayerFootballerMappingSubset("kotekMaskotek"));
    }
    @Test
    public void readyForGame() {
        proxyServerClient.writeAndRead(2, mockFactory.readyForGame());
    }
    @Test
    public void goBackToHostingGame() {
        proxyServerClient.writeAndRead(2, mockFactory.goBackToHostingGame());
    }
    @Test
    public void loginAndReadyForGame() {
        hostGameByKotekMaskotek();
        setTeamHexagoniaFlyiers();
        readyForGame();
    }
}
