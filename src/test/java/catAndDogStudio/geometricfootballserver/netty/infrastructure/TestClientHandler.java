package catAndDogStudio.geometricfootballserver.netty.infrastructure;

import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
public class TestClientHandler extends SimpleChannelInboundHandler<GeometricFootballResponse.Response> {

    final Queue<GeometricFootballResponse.Response> responses;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, GeometricFootballResponse.Response msg) throws Exception {
        log.info("Message received: " + msg);
        responses.add(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
