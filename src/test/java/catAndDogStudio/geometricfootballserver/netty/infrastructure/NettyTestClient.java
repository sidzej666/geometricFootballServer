package catAndDogStudio.geometricfootballserver.netty.infrastructure;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.annotation.Timed;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class NettyTestClient {

    private final int port;
    private final Queue<GeometricFootballResponse.Response> responses = new LinkedBlockingDeque<>();
    Channel channel;
    EventLoopGroup group;

    public void run() {
        group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.AUTO_READ, true)
                    .handler(new TestClientInitializer(responses));

            channel = bootstrap.connect("localhost", port).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(final GeometricFootballRequest.Request request) {
        log.info("Sending message: " + request);
        channel.writeAndFlush(request);
    }

    public GeometricFootballResponse.Response receive() throws InterruptedException {
        GeometricFootballResponse.Response response;
        long waitInterval = 1L;
        long maxWaitForResponse = 1000L;
        long numberOfWaitTicks = maxWaitForResponse / waitInterval;
        long currentWaitTick = 0L;
        do {
            TimeUnit.MILLISECONDS.sleep(waitInterval);
            response = responses.poll();
            currentWaitTick++;
        } while (response == null && currentWaitTick <= numberOfWaitTicks);

        if (response == null) {
            throw new RuntimeException("maxWaitingForResponse time reached and response is still null");
        }
        return response;
    }

    public void shutdown() {
        group.shutdownGracefully();
    }
}
