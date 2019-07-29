package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.DogServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class RunTestServer {

    @Autowired
    private DogServer dogServer;
    private Thread serverThread;

    @PostConstruct
    public void startServerForTests() {
        serverThread = new Thread() {
            public void run(){
                dogServer.start();
            }
        };
        serverThread.start();
    }

    @PreDestroy
    public void killServerAfterTests() {
        serverThread.interrupt();
    }
}
