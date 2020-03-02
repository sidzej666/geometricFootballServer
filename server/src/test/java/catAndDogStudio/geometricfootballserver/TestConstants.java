package catAndDogStudio.geometricfootballserver;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
@Getter
public class TestConstants {
    @Value("${port}")
    private int port;

    @Value("${nettyPort}")
    private int nettyPort;

    @Value("${proxy.port}")
    private int realServerProxyPort;

    @Autowired
    private Environment env;

    public int getPortForTests() {
        if (Arrays.stream(env.getActiveProfiles()).anyMatch(p -> "testServer".equals(p))) {
            return port;
        } else if (Arrays.stream(env.getActiveProfiles()).anyMatch(p -> "nettyTestServer".equals(p))) {
            return nettyPort;
        }
        else if(Arrays.stream(env.getActiveProfiles()).anyMatch(p -> "callRealServerProxy".equals(p))) {
            return realServerProxyPort;
        }
        throw new RuntimeException("no profile - port mapping for tests, current active profiles: " + env.getActiveProfiles());
    }
}
