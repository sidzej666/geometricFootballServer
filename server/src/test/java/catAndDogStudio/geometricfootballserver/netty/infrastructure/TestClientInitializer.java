package catAndDogStudio.geometricfootballserver.netty.infrastructure;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import lombok.RequiredArgsConstructor;

import java.nio.ByteOrder;
import java.util.Queue;

@RequiredArgsConstructor
public class TestClientInitializer extends ChannelInitializer<SocketChannel> {

    final Queue<GeometricFootballResponse.Response> responses;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 4096, 0, 4, 0, 4, false));
        pipeline.addLast(new ProtobufDecoder(GeometricFootballResponse.Response.getDefaultInstance()));
        pipeline.addLast(new LengthFieldPrepender(ByteOrder.LITTLE_ENDIAN, 4, 0, false));
        pipeline.addLast(new ProtobufEncoder());

        pipeline.addLast(new TestClientHandler(responses));
    }
}
