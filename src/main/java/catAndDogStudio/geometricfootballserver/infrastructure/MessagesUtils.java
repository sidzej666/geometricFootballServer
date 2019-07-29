package catAndDogStudio.geometricfootballserver.infrastructure;

import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.InputMessages;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

@Slf4j
public class MessagesUtils {
    private final static long MILLINS_IN_SECOND = 1000L;
    public static String[] writeAndRead(Socket socket, String message) throws Exception {
        write(socket, message);
        final String[] serverResponse = read(socket);
        log.info("Message: " + message + " | Server response: " + serverResponse);
        return serverResponse;
    }
    public static void write(Socket socket, String message) throws Exception {
        socket.getOutputStream().write(message.getBytes());
        log.info("Message: " + message);
    }
    public static String[] read(Socket socket) throws Exception {
        final String[] responses = new String[20];
        int currentResponsesIndex = 0;
        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String serverResponse = null;
        boolean exitCondition = false;
        long threadTime = 0L;
        long loopInterval = 10L;
        long timeoutInterval = 1 * MILLINS_IN_SECOND;
        while (!exitCondition) {
            if(in.ready()) {
                while (in.ready() && (serverResponse = in.readLine()) != null){
                    log.debug("Server response in loop: " + serverResponse);
                    if (!InputMessages.HEARTHBEAT.equals(serverResponse)) {
                        responses[currentResponsesIndex] = serverResponse;
                        currentResponsesIndex++;
                    }
                }
                exitCondition = true;
            }
            Thread.sleep(10L);
            threadTime += loopInterval;
            if (threadTime > timeoutInterval) {
                throw new IllegalStateException("To long waiting for server response, test ERROR");
            }
        }
        //log.info("Server message: " + responses);
        return responses;
    }
}
