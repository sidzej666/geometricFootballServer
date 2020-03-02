package catAndDogStudio.geometricfootballserver.infrastructure.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.GeometricService;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.LeaveGameService;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GeometricFootballServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelGroup allClients;
    private final ServerState serverState;
    private final GeometricService geometricService;
    private final LeaveGameService leaveGameService;
    @Value( "${timeoutInSeconds}" )
    private long readTimeout;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();

        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 4096, 0, 4, 0, 4, false));
        channelPipeline.addLast(new ProtobufDecoder(GeometricFootballRequest.Request.getDefaultInstance()));
        channelPipeline.addLast(new LengthFieldPrepender(ByteOrder.LITTLE_ENDIAN, 4, 0, false));
        channelPipeline.addLast(new ProtobufEncoder());
        channelPipeline.addLast(new ReadTimeoutHandler(readTimeout, TimeUnit.SECONDS));

        channelPipeline.addLast(new GeometricFootballServerHandler(allClients, geometricService, serverState, leaveGameService));
    }
}
