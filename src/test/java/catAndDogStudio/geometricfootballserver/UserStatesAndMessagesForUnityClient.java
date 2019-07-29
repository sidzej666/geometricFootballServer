package catAndDogStudio.geometricfootballserver;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

//@RunWith(SpringRunner.class)
@ActiveProfiles("doNotRunWhileBuild")
@Ignore
public class UserStatesAndMessagesForUnityClient {

    private ProxyServerClient proxyServerClient = new ProxyServerClient("http://localhost:8081/geometricServer");

    @Before
    public void before(){
        //dogServerTestProxy.setUp();
        //environment.setActiveProfiles("callRealServerProxy");
    }

    @Test
    public void resetClients() throws Exception {
        proxyServerClient.resetClient(0);
        proxyServerClient.resetClient(1);
    }

    @Test
    public void awaitGameByPiesek() throws Exception {
        proxyServerClient.writeAndRead(0, ClientMessages.authenticate("1"));
        proxyServerClient.writeAndRead(0, ClientMessages.awaitGame("piesek", "RED"));
    }

    @Test
    public void joinGameByPiesekToHostMysio() throws Exception {
        awaitGameByPiesek();
        proxyServerClient.writeAndRead(0, ClientMessages.joinGameRequest("mysio"));
    }

    @Test
    public void hostGameByMysio() throws Exception {
        proxyServerClient.writeAndRead(1, ClientMessages.authenticate("2"));
        proxyServerClient.writeAndRead(1, ClientMessages.hostGame("mysio", "BLUE"));
        setTeamDataByHostPiesek();
    }

    @Test
    public void acceptPiesekInvitationByMysio() throws Exception {
        proxyServerClient.writeAndRead(1, ClientMessages.acceptGuestInvitation("piesek", "GREY"));
        setTeamDataByHostPiesek();
    }

    @Test
    public void sendAsHostMysioInvitationToPiesek() throws Exception {
        proxyServerClient.writeAndRead(1, ClientMessages.invitePlayer("piesek", "BLACK"));
    }

    @Test
    public void setTeamDataByHostPiesek() throws Exception {
        proxyServerClient.writeAndRead(1, ClientMessages.setTeam());
        proxyServerClient.writeAndRead(1, ClientMessages.setTeamPlayers());
        proxyServerClient.writeAndRead(1, ClientMessages.setTeamTactic());
        proxyServerClient.writeAndRead(1, ClientMessages.setTacticMapping());
        proxyServerClient.writeAndRead(1, ClientMessages.setPlayerMappings("a8dab18d-572b-4a5a-91bd-6e75abda6234", "mysio",
                "c78a99da-899a-40d1-bad7-5816b41e8f07", "mysio", "8f3b10ed-2663-4946-8dbe-9553f83736ca", "mysio",
                "42cda9fb-e418-404b-96e1-455e999215d4", "mysio", "8d9e37fa-2e59-4281-a254-d9e00e1e3ee5", "mysio"));
    }
    @Test
    public void setOnePlayerTacticMappingToMysioByHostPiesek() throws Exception {
        proxyServerClient.writeAndRead(1, ClientMessages.setPlayerMappings("a8dab18d-572b-4a5a-91bd-6e75abda6234", "mysio"));
    }
    @Test
    public void setOnePlayerTacticMappingBackToPiesekByHostPiesek() throws Exception {
        proxyServerClient.writeAndRead(1, ClientMessages.setPlayerMappings("a8dab18d-572b-4a5a-91bd-6e75abda6234", "piesek"));
    }
}
