package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ClientMessages {
    public static String authenticate(String key) {
        return "MAU;" + key + Constants.END_MESSAGE_MARKER;
    }

    public static String awaitGame() {
        return "AWAIT_GAME" + Constants.END_MESSAGE_MARKER;
    }
    public static String writeAndRead(Socket socket, String message) throws IOException {
        socket.getOutputStream().write(message.getBytes());
        byte[] result = new byte[1000];
        socket.getInputStream().read(result);
        log.info("Message: " + message + " | Server response: ", result.toString());
        return result.toString();
    }
    public static String read(Socket socket) throws IOException {
        byte[] result = new byte[1000];
        socket.getInputStream().read(result);
        log.info("Server message: ", result.toString());
        return result.toString();
    }
}
