package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

//@RestController("")
//@RequiredArgsConstructor
//@Profile("proxyForTests")
public class ProxyController {
    //private final DogServerTestProxy dogServerTestProxy;

    //@PostMapping("/client/{clientId}/writeAndRead")
    //public String[] writeAndRead(@PathVariable("clientId") int clientId,
    //        @RequestBody String message) throws Exception {
    //    return dogServerTestProxy.writeAndRead(clientId, message);
    //}

    //@GetMapping
    public String get() {
        return "connected";
    }
}