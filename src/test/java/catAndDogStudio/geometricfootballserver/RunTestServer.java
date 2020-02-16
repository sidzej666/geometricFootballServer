package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.DogServer;
import catAndDogStudio.geometricfootballserver.infrastructure.netty.GeometricFootballServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class RunTestServer {

    @Autowired
    private DogServer dogServer;
    @Autowired
    private GeometricFootballServer geometricFootballServer;
    private Thread serverThread;

    //@PostConstruct
    public void startServerForTests() {
        serverThread = new Thread() {
            public void run(){
                dogServer.start();
            }
        };
        serverThread.start();
    }

    @PostConstruct
    public void startGeometricServerForTests() {
        serverThread = new Thread() {
            public void run(){
                geometricFootballServer.run();
            }
        };
        serverThread.start();
    }

    @PreDestroy
    public void killServerAfterTests() {
        serverThread.interrupt();
    }
}
