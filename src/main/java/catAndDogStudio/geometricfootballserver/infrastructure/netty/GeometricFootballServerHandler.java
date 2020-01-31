package catAndDogStudio.geometricfootballserver.infrastructure.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.MessageHandlingStrategy;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballRequest;
import com.cat_and_dog_studio.geometric_football.protocol.GeometricFootballResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
public class GeometricFootballServerHandler extends SimpleChannelInboundHandler<GeometricFootballRequest.Request> {

    private final ChannelGroup allClients;
    private final MessageHandlingStrategy messageHandlingStrategy;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        final Channel newCat = ctx.channel();
        allClients.add(newCat);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        final Channel leavingCat = ctx.channel();
        allClients.remove(leavingCat);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, GeometricFootballRequest.Request msg) throws Exception {
        System.out.println("message received: " + msg);
        ctx.channel().writeAndFlush(createResponse());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private GeometricFootballResponse.Response createResponse() {
        final GeometricFootballResponse.Response response = GeometricFootballResponse.Response.newBuilder()
                .setType(GeometricFootballResponse.ResponseType.MAU)
                .setMauResponse(GeometricFootballResponse.MauResponse.newBuilder()
                        .setCatName("server-kot")
                        .setMau("Witaj client kocie, tutaj server kot, mau!")
                        .build())
                .build();
        //log.info("returned message: " + response.toByteArray().length + " " + bytesAsString(response.toByteArray()));
        return response;
    }
    
    private String bytesAsString(final byte[] bytes) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (byte znak: bytes) {
            stringBuilder.append(znak);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }
}
