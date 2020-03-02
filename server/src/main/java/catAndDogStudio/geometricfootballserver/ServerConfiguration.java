package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.DogServer;
import catAndDogStudio.geometricfootballserver.infrastructure.netty.GeometricFootballServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
@Profile("server")
public class ServerConfiguration {

    //@Autowired
    private DogServer dogServer;
    @Autowired
    private GeometricFootballServer geometricFootballServer;
    //@PostConstruct
    public void startServer() {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                dogServer.start();
            }
        };
        thread.run();
    }

    @PostConstruct
    public void startNettyServer() {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                geometricFootballServer.run();
            }
        };
        thread.run();
    }
}
