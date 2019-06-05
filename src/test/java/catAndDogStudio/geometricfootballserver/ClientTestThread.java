package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.CatClient;
import lombok.Builder;

@Builder
public class ClientTestThread extends Thread {
    private final String clientId;
    public void run() {
        CatClient catClient = new CatClient(TestConstants.port, clientId);
        catClient.start();
        catClient.send("hello");
        catClient.send("bye");
        catClient.close();
    }
}
