package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.Builder;
import lombok.Getter;

import java.net.Socket;

@Builder
@Getter
public class ProxySocket {
    private final Socket socket;
    private final int clientNumber;

}
