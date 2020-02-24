package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.clients.Message;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

//@Configuration
@Slf4j
public class ProxyServerClient {
    @Value("${clients.proxyTestServer}")
    private String url;
    RestTemplate restTemplate = new RestTemplate();

    public ProxyServerClient(String url) {
        this.url = url;
    }

    public GeometricFootballResponse.Response writeAndRead(int clientId, GeometricFootballRequest.Request request) {
        final Message result =
                restTemplate.postForObject(getUrl(clientId) + "/writeAndRead",
                        Message.builder().message(request.toByteArray()).build(),
                        Message.class);
        try {
            log.info("Response: " + GeometricFootballResponse.Response.parseFrom(result.getMessage()));
            return GeometricFootballResponse.Response.parseFrom(result.getMessage());
        } catch (final InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
    public GeometricFootballResponse.Response read(int clientId) {
        final Message result = restTemplate.postForObject(getUrl(clientId) + "/read",
                null,
                Message.class);
        try {
            log.info("Response: " + GeometricFootballResponse.Response.parseFrom(result.getMessage()));
            return GeometricFootballResponse.Response.parseFrom(result.getMessage());
        } catch (final InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
    public void clearPendingMessage(int clientId) {
        restTemplate.exchange(getUrl(clientId) + "/clearPendingMessage", HttpMethod.POST, null, Void.class);
    }
    public void resetClient(int clientId) {
        restTemplate.exchange(getUrl(clientId) + "/resetClient", HttpMethod.POST, null, Void.class);
    }

    private String getUrl(int clientId) {
        return url + "/client/" + clientId;
    }
    private String recreateClientsUrl() {
        return url + "/recreateClients";
    }

    public void recreateClients() {
        restTemplate.postForEntity(recreateClientsUrl(), HttpEntity.EMPTY, Void.class);
    }
}
