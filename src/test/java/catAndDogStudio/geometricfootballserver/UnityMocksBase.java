package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.mocks.MockFactory;
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
