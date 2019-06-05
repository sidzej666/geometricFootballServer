package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.CatClient;
import catAndDogStudio.geometricfootballserver.infrastructure.DogServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import javax.net.SocketFactory;
import java.net.Socket;

@RunWith(SpringRunner.class)
@Slf4j
public class ClientTest {

    @Test
    public void test() throws Exception {
        final boolean runServer = false;

        if (runServer) {
            (new Thread() {
                @Override
                public void run() {
                    DogServer.builder().port(TestConstants.port).build().start();
                }
            }).start();
        }

        Thread.sleep(1000l);
        for (int i = 0; i < 10000; i++) {
            ClientTestThread clientTestThread = ClientTestThread.builder().clientId("" + i).build();
            clientTestThread.start();
        }
    }
    @Test
    public void socketTest() throws Exception {
        Socket socket = SocketFactory.getDefault().createSocket("localhost", TestConstants.port);
        socket.getOutputStream().write("NEW_2GAME;java-kotek:END".getBytes());
        socket.getOutputStream().write("NEW_GAME;java-kotek:END".getBytes());
        byte[] result = new byte[1000];
        socket.getInputStream().read(result);
        log.info("Server response: ", result);
        socket.close();
    }

    @Test
    public void awaitGame() throws Exception {
        Socket socket = SocketFactory.getDefault().createSocket("localhost", TestConstants.port);
        ClientMessages.writeAndRead(socket, ClientMessages.authenticate("0987654321"));
        ClientMessages.writeAndRead(socket, ClientMessages.awaitGame());
        while(true) {
            ClientMessages.read(socket);
        }
    }

    //@Test
    public void createGame() throws Exception {
        CatClient catClient = new CatClient(TestConstants.port, "java-kotek");
        catClient.start();
        catClient.send("NEW_GAME;java-kotek:END");
        catClient.close();
    }
}
