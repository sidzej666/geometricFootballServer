package catAndDogStudio.geometricfootballserver.infrastructure;

import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.LeaveGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
//@Service
public class DogServer implements ChannelWriter {
    private static final int BUFFER_SIZE = 1024;
    private static final long MAX_CONNECTIONS = 100_000l;

    private final LeaveGameService leaveGameService;

    @Value("${port}")
    private int port;
    private final Map<SocketChannel, StringBuilder> session = new HashMap<>();
    private final Map<SelectableChannel, Game> gameObjectForClient = new HashMap<>();
    private final GeometricService geometricService;
    private final GamePool gamePool = new GamePool(MAX_CONNECTIONS);

    public void start() {
        try (Selector selector = Selector.open(); ServerSocketChannel channel = ServerSocketChannel.open()) {
            initChannel(channel, selector);
            while (!Thread.currentThread().isInterrupted()) {
                //log.info("active clients: {}", session);
                if (selector.isOpen()) {
                    final int numKeys = selector.select();
                    if (numKeys > 0) {
                        handleKeys(channel, selector.selectedKeys());
                    }
                } else {
                    Thread.currentThread().interrupt();
                }
                final long currentTime = new Date().getTime();

                final Set<SelectableChannel> channelsToRemove = new HashSet<SelectableChannel>();
                for(final SelectableChannel c : gameObjectForClient.keySet()) {
                    try {
                        geometricService.checkIfSendHeathBeatMessageAndSendItIfIsNeeded(c, currentTime, gameObjectForClient.get(c));
                        if (!c.isOpen()) {
                            channelsToRemove.add(c);
                        }
                    } catch (IOException e) {
                        channelsToRemove.add(c);
                    }
                }
                for(SelectableChannel c : channelsToRemove) {
                    if (gameObjectForClient.get(c).getOwnerName() != null) {
                        log.warn("Client disconnected {}", gameObjectForClient.get(c).getOwnerName());
                    }
                    //leaveGameService.leaveGame(c, gameObjectForClient.get(c), true);
                    session.remove(c);
                    gameObjectForClient.remove(c);
                    geometricService.remove(c);
                    c.close();
                }
            }
        } catch (IOException e) {
            log.info("IOException", e);
        } finally {
            this.session.clear();
        }
    }

    private void initChannel(final ServerSocketChannel channel, final Selector selector) throws IOException {
        assert !Objects.isNull(channel) && !Objects.isNull(selector);

        channel.socket().setReuseAddress(true);
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(this.port));
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void handleKeys(final ServerSocketChannel channel, final Set<SelectionKey> keys) throws IOException {
        assert !Objects.isNull(keys) && !Objects.isNull(channel);

        final Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {

            final SelectionKey key = iterator.next();
            try {
                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        doAccept(channel, key);
                    } else if (key.isReadable()) {
                        doRead(key);
                    } else {
                        throw new UnsupportedOperationException("Key not supported by server.");
                    }
                } else {
                    throw new UnsupportedOperationException("Key not valid.");
                }
            } finally {
                if (mustEcho(key)) {
                    doEcho(key);
                    this.session.get(key.channel()).delete(0, this.session.get(key.channel()).length());
                    //cleanUp(key);
                }

                iterator.remove();
            }
        }
    }

    private void doAccept(final ServerSocketChannel channel, final SelectionKey key) throws IOException {
        assert !Objects.isNull(key) && !Objects.isNull(channel);

        final SocketChannel client = channel.accept();
        client.configureBlocking(false);
        client.register(key.selector(), SelectionKey.OP_READ);
        final Game newGame = gamePool.getGame();
        newGame.setPlayerState(PlayerState.AWAITING_AUTHENTICATION);
        newGame.setLastHearthBeatTime(new Date().getTime());
        geometricService.awaitAuthentication(client, newGame);
        gameObjectForClient.put(client, newGame);
        // Create a session for the incoming connection
        this.session.put(client, new StringBuilder());
    }

    private void doRead(final SelectionKey key) throws IOException {
        assert !Objects.isNull(key);

        final SocketChannel client = (SocketChannel) key.channel();
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        try {
            final int bytesRead = client.read(buffer);
            if (bytesRead > 0) {
                this.session.get(client).append(new String(buffer.array()).trim());
            } else if (bytesRead < 0) {
                if (mustEcho(key)) {
                    doEcho(key);

                }
                //cleanUp(key);
            }
        } catch (Exception e) {
            client.close();
        }
    }

    private void doEcho(final SelectionKey key) throws IOException {
        assert !Objects.isNull(key);

        String messageFromClient = this.session.get(key.channel()).toString().trim();
        if (!messageFromClient.contains(Constants.END_MESSAGE_MARKER)) {
            geometricService.storePartialMessage(key.channel(), gameObjectForClient.get(key.channel()), messageFromClient);
            return;
        }
        Game game = gameObjectForClient.get(key.channel());
        String fullMessage = (game.getPartialMessage() == null ? "" : game.getPartialMessage()) + messageFromClient;
        final boolean lastPartIsPartialMessage = !fullMessage.endsWith(Constants.END_MESSAGE_MARKER);
        String[] messagesFromClient = fullMessage.split(Constants.END_MESSAGE_MARKER);
        game.setPartialMessage(null);
        try {
            for(int i = 0; i < messagesFromClient.length; i++) {
                if (lastPartIsPartialMessage && i == messagesFromClient.length - 1) {
                    geometricService.storePartialMessage(key.channel(), gameObjectForClient.get(key.channel()),
                            messagesFromClient[i]);
                    return;
                }
                /*
                geometricService.handleMessage(key.channel(),
                        gameObjectForClient.get(key.channel()), messagesFromClient[i]);
                 */
            }
        } catch (Exception e) {
            log.warn("Exception while handling message: " + messageFromClient, e);
        }
    }

    private boolean mustEcho(final SelectionKey key) {
        assert !Objects.isNull(key);

        return (key.channel() instanceof SocketChannel) && this.session.get((SocketChannel) key.channel()).toString().contains(Constants.END_MESSAGE_MARKER);
    }

    private void cleanUp(final SelectionKey key) throws IOException {
        assert !Objects.isNull(key);

        this.session.remove((SocketChannel) key.channel());

        key.channel().close();
        key.cancel();
    }
}
