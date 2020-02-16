package catAndDogStudio.geometricfootballserver.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "proxyTestServer", url = "${clients.proxyTestServer}")
public interface ProxyTestServer {
    @RequestMapping(method = RequestMethod.POST, value = "/client/{clientId}/readAndWrite")
    Message readAndWrite(@PathVariable("clientId") int clientId, @RequestBody Message message);
}
