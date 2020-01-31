package catAndDogStudio.geometricfootballserver.infrastructure.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.MessageHandlingStrategy;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.ByteOrder;

@Component
@RequiredArgsConstructor
public class GeometricFootballServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelGroup allClients;
    private final MessageHandlingStrategy messageHandlingStrategy;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();

        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 4096, 0, 4, 0, 4, false));
        channelPipeline.addLast(new ProtobufDecoder(GeometricFootballRequest.Request.getDefaultInstance()));
        channelPipeline.addLast(new LengthFieldPrepender(ByteOrder.LITTLE_ENDIAN, 4, 0, false));
        channelPipeline.addLast(new ProtobufEncoder());

        channelPipeline.addLast(new GeometricFootballServerHandler(allClients, messageHandlingStrategy));
    }
}
