package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.clients.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ProxyServerClient {
    @Value("${clients.proxyTestServer}")
    private String url;
    RestTemplate restTemplate = new RestTemplate();

    public ProxyServerClient(String url) {
        this.url = url;
    }

    public String[] writeAndRead(int clientId, String message) {
        final List<String> result =  ((ArrayList<Object>)restTemplate.postForObject(getUrl(clientId) + "/writeAndRead", Message.builder().message(message).build(), Object.class))
                .stream()
                .map(m -> (String) m)
                .collect(Collectors.toList());
        log.info("Response: " + result);
        return result.toArray(new String[result.size()]);
    }
    public void resetClient(int clientId) {
        restTemplate.exchange(getUrl(clientId) + "/resetClient", HttpMethod.POST, null, Void.class);
    }

    private String getUrl(int clientId) {
        return url + "/client/" + clientId;
    }
}
