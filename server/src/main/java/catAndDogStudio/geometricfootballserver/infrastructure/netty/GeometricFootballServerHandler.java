package catAndDogStudio.geometricfootballserver.infrastructure.netty;

import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.GeometricService;
import catAndDogStudio.geometricfootballserver.infrastructure.ServerState;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.LeaveGameService;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.netty.MessageHandlingStrategy;
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
    private final GeometricService geometricService;
    private final ServerState serverState;
    private final LeaveGameService leaveGameService;
    Game game;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        game = new Game();
        game.setChannel(ctx.channel());
        final Channel newCat = ctx.channel();
        allClients.add(newCat);
        serverState.addGame(newCat.id(), game);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        final Channel leavingCat = ctx.channel();
        log.info("handler removed: {}" + leavingCat.id());
        Game game = serverState.getGames().get(leavingCat.id());
        leaveGameService.leaveGame(leavingCat, game, true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, GeometricFootballRequest.Request request) throws Exception {
        //System.out.println("message received: " + request);
        geometricService.handleMessage(ctx.channel(), game, request);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}
