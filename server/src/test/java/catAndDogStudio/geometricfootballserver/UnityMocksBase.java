package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.mocks.MockFactory;
import catAndDogStudio.geometricfootballserver.mocks.MockPlayerFactory;
import catAndDogStudio.geometricfootballserver.mocks.MockTeamFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("doNotRunWhileBuild")
@Ignore
public class UnityMocksBase {
    private final String url = "http://localhost:8081/geometricServer";
    protected ProxyServerClient proxyServerClient = new ProxyServerClient(url);
    protected final MockFactory mockFactory = new MockFactory();
    protected final MockPlayerFactory mockPlayerFactory = new MockPlayerFactory();
    protected final MockTeamFactory mockTeamFactory = new MockTeamFactory(mockPlayerFactory);

    public void resetClients() throws Exception {
        proxyServerClient.resetClient(0);
        proxyServerClient.resetClient(1);
        proxyServerClient.resetClient(2);
        proxyServerClient.resetClient(3);
    }

    public void recreateClients() throws Exception {
        proxyServerClient.recreateClients();
    }
}
