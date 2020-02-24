package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.mocks.MockFactory;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest.TeamInvitationAction;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest.TeamInvitationDirection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

public class UserStatesAndMessagesForUnityClient extends UnityMocksBase {

    @Test
    public void authenticateIn4CLients() throws Exception {
        proxyServerClient.writeAndRead(0, mockFactory.authenticate("piesek", "1"));
        proxyServerClient.writeAndRead(1, mockFactory.authenticate("kotekMaskotek", "1234567890"));
        proxyServerClient.writeAndRead(2, mockFactory.authenticate("javaKotek", "0987654321"));
        proxyServerClient.writeAndRead(3, mockFactory.authenticate("mysio", "2"));
    }

    @Test
    public void awaitGameByMysio() throws Exception {
        //proxyServerClient.writeAndRead(1, ClientMessages.authenticate("2"));
        //proxyServerClient.writeAndRead(1, ClientMessages.awaitGame("mysio", "BLUE"));
    }
    @Test
    public void joinGameByMysioToHostPiesek() throws Exception {
        awaitGameByMysio();
        //proxyServerClient.writeAndRead(1, ClientMessages.joinGameRequest("piesek"));
    }


    @Test
    public void awaitOpponentsByHostJavaKotekWithHexagoniaFlyers() throws Exception {
        //proxyServerClient.writeAndRead(2, ClientMessages.authenticate("0987654321"));
        //proxyServerClient.writeAndRead(2, ClientMessages.hostGame("javaKotek", "BLUE"));
        setTeamDataByHostJavaKotek();
        //proxyServerClient.writeAndRead(2, ClientMessages.readyForGame());
    }

    @Test
    public void acceptPiesekInvitationByMysio() throws Exception {
        //proxyServerClient.writeAndRead(1, ClientMessages.acceptGuestInvitation("piesek", "GREY"));
        setTeamDataByHostMysioToGuestPiesek();
    }

    @Test
    public void sendAsHostMysioInvitationToPiesek() throws Exception {
        //proxyServerClient.writeAndRead(1, ClientMessages.invitePlayer("piesek", "BLACK"));
    }

    @Test
    public void setTeamDataByHostMysioToGuestPiesek() throws Exception {
        //proxyServerClient.writeAndRead(1, ClientMessages.setTeamFcVegetables());
        //proxyServerClient.writeAndRead(1, ClientMessages.setTeamPlayersFcVegetables());
        //proxyServerClient.writeAndRead(1, ClientMessages.setTeamTacticFcVegetables());
        //proxyServerClient.writeAndRead(1, ClientMessages.setTacticMappingFcVegetables());
        //proxyServerClient.writeAndRead(1, ClientMessages.setUserToPlayerMappingFcVegetables());
    }
    @Test
    public void setTeamDataByHostJavaKotek() throws Exception {
        //proxyServerClient.writeAndRead(2, ClientMessages.setTeamHexagoniaFlyers());
        //proxyServerClient.writeAndRead(2, ClientMessages.setTeamPlayersHexagoniaFlyers());
        //proxyServerClient.writeAndRead(2, ClientMessages.setTeamTacticHexagoniaFlyers());
        //proxyServerClient.writeAndRead(2, ClientMessages.setTacticMappingHexagoniaFlyers());
        //proxyServerClient.writeAndRead(2, ClientMessages.setUserToPlayerMappingHexagoniaFlyers());
    }
    @Test
    public void setOnePlayerTacticMappingToMysioByHostPiesek() throws Exception {
        //proxyServerClient.writeAndRead(1, ClientMessages.setPlayerMappings("a8dab18d-572b-4a5a-91bd-6e75abda6234", "mysio"));
    }
    @Test
    public void goToWaitingForOponentsByHostMysio() {
        //proxyServerClient.writeAndRead(1, ClientMessages.readyForGame());
    }
    @Test
    public void goBackToTeamCreationByHostMysio() {
        //proxyServerClient.writeAndRead(1, ClientMessages.goBackToTeamCreation());
    }
    @Test
    public void setOnePlayerTacticMappingBackToPiesekByHostPiesek() throws Exception {
        //proxyServerClient.writeAndRead(1, ClientMessages.setPlayerMappings("a8dab18d-572b-4a5a-91bd-6e75abda6234", "piesek"));
    }
}
