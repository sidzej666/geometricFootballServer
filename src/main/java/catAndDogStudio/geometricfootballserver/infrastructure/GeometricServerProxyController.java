package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/geometricServer")
@RequiredArgsConstructor
public class GeometricServerProxyController {
    //private final DogServerTestProxy dogServerTestProxy;

    @PostMapping("/client/{clientId}/writeAndRead")
    public String[] writeAndRead(@PathVariable("clientId") int clientId,
           @RequestBody String message) throws Exception {
        return null;
      //return dogServerTestProxy.writeAndRead(clientId, message.getMessage());
    }

    @GetMapping("/")
    public String get() {
        return "Geometric Server proxt is alive";
    }
}