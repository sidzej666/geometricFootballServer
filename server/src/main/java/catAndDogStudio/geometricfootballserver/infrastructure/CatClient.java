package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;

@Slf4j
public class CatClient implements ChannelWriter {
    private final InetSocketAddress hostAddress;
    private SocketChannel client;
    @Getter
    private final String clientId;

    public CatClient(final int port, final String clientId) {
        this.hostAddress = new InetSocketAddress(port);
        this.clientId = clientId;
    }

    public void start()  {
        try {
            client = SocketChannel.open(this.hostAddress);
            final ByteBuffer buffer = ByteBuffer.wrap(("CONNECT" + Constants.END_MESSAGE_MARKER).trim().getBytes());

            doWrite(buffer, client);

            buffer.flip();

            final StringBuilder echo = new StringBuilder();
            doRead(echo, buffer, client);


            //log.info(String.format("Starting client %s. Server response: %s", clientId,
            //        echo.toString().replace(Constants.END_MESSAGE_MARKER, Constants.EMPTY_STRING)));
        } catch (IOException e) {
            throw new RuntimeException("Unable to communicate with server.", e);
        }
    }

    public String send(String message) {
        try {

            final ByteBuffer buffer = ByteBuffer.wrap((message + Constants.END_MESSAGE_MARKER).trim().getBytes());

            doWrite(buffer, client);

            buffer.flip();

            final StringBuilder echo = new StringBuilder();
            doRead(echo, buffer, client);

            final String response = echo.toString().replace(Constants.END_MESSAGE_MARKER, Constants.EMPTY_STRING);
            log.info(String.format("ClientId: %s Message: %s Response: %s", clientId, message, response));
            return response;
        } catch (IOException e) {
            throw new RuntimeException("Unable to communicate with server.", e);
        }
    }

    public void close() {
        try {
            client.close();
        } catch (Exception e) {
            log.error("Closing connection error", e);
        }
    }

    private void doRead(final StringBuilder data, final ByteBuffer buffer, final SocketChannel channel) throws IOException {
        assert !Objects.isNull(data) && !Objects.isNull(buffer) && !Objects.isNull(channel);

        while (channel.read(buffer) != -1) {
            data.append(new String(buffer.array()).trim());
            buffer.clear();
        }
    }
}
