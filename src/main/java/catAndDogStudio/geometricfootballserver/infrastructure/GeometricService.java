package catAndDogStudio.geometricfootballserver.infrastructure;

import catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers.InputMessages;
import catAndDogStudio.geometricfootballserver.infrastructure.messagesHandlers.OutputMessages;
import com.sun.org.apache.bcel.internal.generic.Select;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class GeometricService implements ChannelWriter {
    private static final long DISCONNECTION_CHECK_INTERVAL = 5000l;
    private final Map<SelectableChannel, Game> awaitingAuthentication = new HashMap<>();
    private final Map<SelectableChannel, Game> hostedGames = new HashMap<>();
    private final Map<SelectableChannel, Game> waitingForGames = new HashMap<>();
    private final Map<SelectableChannel, Game> playersInGame = new HashMap<>();

    private final ServerState serverState;
    private final MessageHandlingStrategy messageHandlingStrategy;

    public void remove(SelectableChannel s) {
        hostedGames.remove(s);
        awaitingAuthentication.remove(s);
        waitingForGames.remove(s);
        Game game = playersInGame.get(s);
        if (game != null) {
            game.getPlayersInGame().remove(s);
            playersInGame.remove(s);
            sendPlayerLeftGameAndHandleHostDisconnection(game, s);
        }
    }

    //TODO: strategy + implementations that handle messages
    public String handleMessage(SelectableChannel channel, Game game, String message) {
        messageHandlingStrategy.handleMessage(channel, game, message);

        if (message.startsWith(InputMessages.INVITE_PLAYER)) {
            return createInvitationAndInvitePlayer(channel, message.split(";")[1]);
        }
        if (message.startsWith(InputMessages.JOIN_GAME)) {
            return askForGameJoin(channel, game, message);
        }
        if (message.startsWith(InputMessages.MAU)) {
            return tryToAuthenticate(channel, game, message);
        }
        if (message.startsWith(InputMessages.HOST_GAME)) {
            return hostGame(channel, game, message);
        }
        if (message.startsWith(InputMessages.AWAIT_GAME)) {
            return awaitGame(channel, game, message);
        }
        if (message.startsWith(InputMessages.LEAVE_GAME)) {
            return leaveGame(channel, game, message);
        }
        if (message.startsWith(InputMessages.INVITATION_ACCEPTED_BY_HOST)) {
            return invitationAcceptedByHost(channel, game, message);
        }
        if (message.startsWith(InputMessages.INVITATION_REJECTED_BY_HOST)) {
            return invitationRejectedByHost(channel, game, message);
        }
        if (message.startsWith(InputMessages.KITTY_KICKED_BY_HOST_KITTY)) {
            return playerKickedOutByHost(channel, game, message);
        }
        if (message.startsWith(InputMessages.INVITATION_ACCEPTED_BY_GUEST)) {
            return invitationAcceptedByGuest(channel, game, message);
        }
        if (message.startsWith(InputMessages.INVITATION_REJECTED_BY_GUEST)) {
            return invitationRejectedByGuest(channel, game, message);
        }
        if (message.startsWith(InputMessages.CANCEL_INVITATION)) {
            return guestCancelsInvitation(channel, game, message);
        }
        log.warn("Wrong message, can't handle: {}", message);
        return "UNKNOWN_MESSAGE";
    }

    private String guestCancelsInvitation(SelectableChannel channel, Game game, String message) {
        String[] splittedMessage = message.split(";");
        SelectableChannel hostChannel = hostedGames.keySet().stream()
                .filter(k -> hostedGames.get(k).getOwnerName().equals(splittedMessage[1]))
                .findAny()
                .orElse(null);
        if (hostChannel == null) {
            return "INVITATION_NOT_FOUND";
        }
        Invitation invitation = hostedGames.get(hostChannel)
                .getInvitations()
                .stream()
                .filter(i -> i.getInvitator().equals(splittedMessage[1]))
                .filter(i -> i.getInvitedPlayer().equals(game.getOwnerName()))
                .findAny()
                .orElse(null);
        if (invitation == null) {
            return "INVITATION_NOT_FOUND";
        }
        sendInvitationCancelledByGuest(hostChannel, splittedMessage[1]);
        game.setPlayerState(PlayerState.AWAITS_GAME);
        hostedGames.get(hostChannel).getInvitations().remove(invitation);
        return "INVITATION_CANCELLED;" + invitation.getInvitator();
    }

    private void sendInvitationCancelledByGuest(SelectableChannel hostChannel, String guestName) {
        final ByteBuffer response = ByteBuffer.wrap(("INVITATION_CANCELLED;" + guestName
                + Constants.END_MESSAGE_MARKER + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) hostChannel);
        }  catch (IOException e) {
            log.error("Error while sending invitation invitation cancelled by: " + guestName, e);
        }
    }

    private String invitationAcceptedByHost(SelectableChannel channel, Game game, String message) {
        String[] messageParams = message.split(";");
        String guestName = messageParams[1];
        Invitation invitation = game.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(guestName))
                .findAny()
                .orElse(null);
        if (invitation == null) {
            return "NO_SUCH_INVITED_KITTY;" + guestName;
        }
        waitingForGames.get(invitation.getInvitedPlayerChannel()).setPlayerState(PlayerState.GAME_GUEST);
        sendPlayerJoinGameToAllPlayersInGame(game, guestName, invitation.getInvitedPlayerChannel());
        game.getPlayersInGame().put(invitation.getInvitedPlayerChannel(), waitingForGames.get(invitation.getInvitedPlayerChannel()));
        game.getInvitations().remove(invitation);
        waitingForGames.remove(invitation.getInvitedPlayerChannel());
        playersInGame.put(invitation.getInvitedPlayerChannel(), game);
        sentPlayersListToAcceptedPlayer(game, invitation.getInvitedPlayerChannel());
        return "KITTY_JOINED_GAME;" + guestName;
    }

    private void sentPlayersListToAcceptedPlayer(Game game, SelectableChannel acceptedPlayerChannel) {
        String message = "TEAM_PLAYERS;" + game.getOwnerName() + ";";
        for(Game playerInGame : game.getPlayersInGame().values()) {
            message += playerInGame.getOwnerName() + ";";
        }
        final ByteBuffer response = ByteBuffer.wrap((message + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) acceptedPlayerChannel);
        }  catch (IOException e) {
            log.error("Error while sending team players for game " + game.getOwnerName()
                    + ", to: " + playersInGame.get(acceptedPlayerChannel).getOwnerName(), e);
        }
    }

    private String invitationRejectedByHost(SelectableChannel channel, Game game, String message) {
        String[] messageParams = message.split(";");
        String guestName = messageParams[1];
        Invitation invitation = game.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(guestName))
                .findAny()
                .orElse(null);
        if (invitation == null || waitingForGames.get(invitation.getInvitedPlayerChannel()) == null) {
            return "NO_SUCH_INVITED_KITTY;" + guestName;
        }
        waitingForGames.get(invitation.getInvitedPlayerChannel()).setPlayerState(PlayerState.AWAITS_GAME);
        sendPlayerInvitationRejected(game.getOwnerName(), invitation.getInvitedPlayer(), invitation.getInvitedPlayerChannel());
        game.getInvitations().remove(invitation);
        return "KITTY_INVITATION_REJECTED;" + guestName;
    }
    private String playerKickedOutByHost(SelectableChannel channel, Game game, String message) {
        String[] messageParams = message.split(";");
        String guestName = messageParams[1];
        SelectableChannel guestChannel = game.getPlayersInGame().keySet().stream()
                .filter(k -> game.getPlayersInGame().get(k).getOwnerName().equals(guestName))
                .findAny()
                .orElse(null);
        if (guestChannel == null) {
            return "NO_SUCH_KITTY_IN_GAME_KUWETA;" + guestName;
        }
        Game guestGame = game.getPlayersInGame().get(guestChannel);
        waitingForGames.put(guestChannel, guestGame);
        guestGame.setPlayerState(PlayerState.AWAITS_GAME);
        sendPlayerKickedOutToAllPlayersInGame(game, guestName);
        game.getPlayersInGame().remove(guestChannel);
        playersInGame.remove(guestChannel);
        return "KITTY_KICKED_OUT;" + guestName;
    }

    private void sendPlayerKickedOutToAllPlayersInGame(Game game, String guestName) {
        final ByteBuffer response = ByteBuffer.wrap(("KITTY_KICKED_OUT;" + game.getOwnerName() + ";" + guestName
                + Constants.END_MESSAGE_MARKER + "\n").getBytes());
        for (SelectableChannel channel: game.getPlayersInGame().keySet()) {
            try {
                doWrite(response, (SocketChannel) channel);
            }  catch (IOException e) {
                log.error("Error while sending kitty kicked out, gameOwner: " + game.getOwnerName()
                        + ", kicked kitty: " + guestName, e);
            }
        }
    }

    private void sendPlayerInvitationRejected(String ownerName, String guestName, SelectableChannel invitedPlayerChannel) {
        final ByteBuffer response = ByteBuffer.wrap(("KITTY_NOT_WANTED;" + ownerName + ";" + guestName
                + Constants.END_MESSAGE_MARKER + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) invitedPlayerChannel);
        }  catch (IOException e) {
            log.error("Error while sending invitation rejected by: " + ownerName + " to " + guestName, e);
        }
    }

    private void sendPlayerJoinGameToAllPlayersInGame(Game game, String guestName, SelectableChannel guestChannel) {
        final ByteBuffer response = ByteBuffer.wrap(("KITTY_JOINED_GAME;" + guestName
                + Constants.END_MESSAGE_MARKER + "\n").getBytes());
        List<SelectableChannel> channels = new ArrayList<>();
        channels.addAll(game.getPlayersInGame().keySet());
        channels.add(guestChannel);
        for (SelectableChannel channel: channels) {
            try {
                doWrite(response, (SocketChannel) channel);
            }  catch (IOException e) {
                log.error("Error while sending guest: " + guestName + " joined game to "
                        + game.getPlayersInGame().get(channel).getOwnerName(), e);
            }
        }
    }

    private void sendPlayerLeftGameAndHandleHostDisconnection(Game game, SelectableChannel s) {

    }

    private String invitationAcceptedByGuest(SelectableChannel channel, Game game, String message) {
        if (game.getPlayerState() != PlayerState.AWAITS_GAME) {
            return "BAD_KITTY_STATE_FUR_ACCEPTING_GAME_INVITATION;" + game.getOwnerName() + ";" + game.getPlayerState();
        }
        String[] splittedMessage = message.split(";");
        SelectableChannel hostChannel = hostedGames.keySet().stream()
                .filter(c -> hostedGames.get(c).getOwnerName().equals(splittedMessage[1]))
                .findAny()
                .orElse(null);
        if (hostChannel == null) {
            return "NO_SUCH_KITTY;" + splittedMessage[1];
        }
        Game hostGame = hostedGames.get(hostChannel);
        Invitation invitation = hostGame.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(game.getOwnerName()))
                .findAny()
                .orElse(null);
        if (invitation == null) {
            return "NO_SUCH_INVITATION;" + splittedMessage[1] + ";" + game.getOwnerName();
        }
        hostGame.getInvitations().remove(invitation);
        hostGame.getPlayersInGame().put(channel, game);
        game.setPlayerState(PlayerState.GAME_GUEST);
        waitingForGames.remove(channel);
        sendInvitationAcceptedToGameHost(hostChannel, hostGame.getOwnerName(), game.getOwnerName());
        return "INVITATION_ACCEPTED_BY_GUEST;" + splittedMessage[1] + ";" + game.getOwnerName();
    }

    private String invitationRejectedByGuest(SelectableChannel channel, Game game, String message) {
        if (game.getPlayerState() != PlayerState.AWAITS_GAME) {
            return "BAD_KITTY_STATE_FUR_REJECTING_GAME_INVITATION;" + game.getOwnerName() + ";" + game.getPlayerState();
        }
        String[] splittedMessage = message.split(";");
        SelectableChannel hostChannel = hostedGames.keySet().stream()
                .filter(c -> hostedGames.get(c).getOwnerName().equals(splittedMessage[1]))
                .findAny()
                .orElse(null);
        if (hostChannel == null) {
            return "NO_SUCH_KITTY;" + splittedMessage[1];
        }
        Game hostGame = hostedGames.get(hostChannel);
        Invitation invitation = hostGame.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(game.getOwnerName()))
                .findAny()
                .orElse(null);
        if (invitation == null) {
            return "NO_SUCH_INVITATION;" + splittedMessage[1] + ";" + game.getOwnerName();
        }
        hostGame.getInvitations().remove(invitation);
        sendInvitationRejectedToGameHost(hostChannel, hostGame.getOwnerName(), game.getOwnerName());
        return "INVITATION_REJECTED_BY_GUEST;" + splittedMessage[1] + ";" + game.getOwnerName();
    }

    private void sendInvitationRejectedToGameHost(SelectableChannel hostChannel, String hostName, String guestName) {
        final ByteBuffer response = ByteBuffer.wrap((InputMessages.INVITATION_REJECTED_BY_GUEST + ";" + guestName
                + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) hostChannel);
        }  catch (IOException e) {
            log.error("Error while sending INVITATION_REJECTED_BY_GUEST from " + guestName + " to " + hostName, e);
        }
    }
    private void sendInvitationAcceptedToGameHost(SelectableChannel hostChannel, String hostName, String guestName) {
        final ByteBuffer response = ByteBuffer.wrap((InputMessages.INVITATION_ACCEPTED_BY_GUEST + ";" + guestName
                + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) hostChannel);
        }  catch (IOException e) {
            log.error("Error while sending INVITATION_ACCEPTED_BY_GUEST from " + guestName + " to " + hostName, e);
        }
    }

    private String leaveGame(SelectableChannel channel, Game game, String message) {
        if (game.getPlayerState() != PlayerState.AWAITS_GAME
                && game.getPlayerState() != PlayerState.GAME_HOST) {
            return "BAD_CAT_STATE_FOR_LEAVING_GAME";
        }
        if (game.getPlayerState() == PlayerState.GAME_HOST) {
            game.getInvitations().stream()
                    .forEach(i -> sendHostLeft(i));
            game.getInvitations().clear();
        } else if (game.getPlayerState() == PlayerState.AWAITS_GAME) {
            Invitation invitation = game.getInvitations().stream()
                    .filter(i -> i.getInvitedPlayer().equals(game.getOwnerName()))
                    .findFirst()
                    .orElse(null);
            if (invitation != null) {
                game.getInvitations().remove(invitation);
                game.getInvitations().stream()
                        .forEach(i -> sendPlayerLeft(i, invitation.getInvitedPlayer()));
                sendPlayerLeftToHost(invitation);
            }
        }
        game.setPlayerState(PlayerState.AUTHENTICATED);
        hostedGames.remove(channel);
        waitingForGames.remove(channel);
        return "REMOVE_FROM_AWAITING_GAMES;" + game.getOwnerName();
    }

    private void sendHostLeft(Invitation i) {
        final ByteBuffer response = ByteBuffer.wrap((InputMessages.HOST_LEFT + ";" + i.getInvitator()
                + Constants.END_MESSAGE_MARKER + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) i.getInvitedPlayerChannel());
        }  catch (IOException e) {
            log.error("Error while sending host left message invitator: " + i.getInvitator() + " to " + i.getInvitedPlayer(), e);
        }
    }

    private void sendPlayerLeft(Invitation i, String playerName) {
        final ByteBuffer response = ByteBuffer.wrap((InputMessages.PLAYER_LEFT + ";" + playerName
                + Constants.END_MESSAGE_MARKER + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) i.getInvitedPlayerChannel());
        }  catch (IOException e) {
            log.error("Error while sending player left message player: " + playerName + " to " + i.getInvitedPlayer(), e);
        }
    }
    private void sendPlayerLeftToHost(Invitation i) {
        final ByteBuffer response = ByteBuffer.wrap((InputMessages.PLAYER_LEFT + ";" + i.getInvitedPlayer()
                + Constants.END_MESSAGE_MARKER + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) i.getInvitedPlayerChannel());
        }  catch (IOException e) {
            log.error("Error while sending player left message player: " + i.getInvitedPlayer() + " to " + i.getInvitator(), e);
        }
    }

    private String hostGame(SelectableChannel channel, Game game, String message) {
        if (game.getPlayerState() != PlayerState.AUTHENTICATED) {
            return "BAD_CAT_STATE_FOR_HOSTING_GAME";
        }
        if (hostedGames.get(channel) != null) {
            return "CUTE_KITTY_YOU_ARE_ALREADY_HOSTING_A_GAME";
        }
        game.setPlayerState(PlayerState.GAME_HOST);
        hostedGames.put(channel, game);
        return "GAME_HOSTED;" + game.getOwnerName();
    }


    private String tryToAuthenticate(SelectableChannel channel, Game game, String message) {
        if (game.getPlayerState() != PlayerState.AWAITING_AUTHENTICATION
            || !awaitingAuthentication.containsKey(channel)) {
            return "YOU_HAD_ALREADY_MAUED_TO_THE_DOG";
        }
        String key = message.split(";")[1];
        if ("1234567890".equals(key)) {
            game.setOwnerName("kotek-maskotek");
        } else if ("0987654321".equals(key)) {
            game.setOwnerName("java-kotek");
        } else if ("1".equals(key)) {
            game.setOwnerName("piesek");
        } else if ("2".equals(key)) {
            game.setOwnerName("mysio");
        } else {
            return "YOU_ARE_MAUING_IN_A_VERY_STRANGE_WAY_KITTY";
        }
        game.setPlayerState(PlayerState.AUTHENTICATED);
        game.setLastHearthBeatTime(new Date().getTime());
        awaitingAuthentication.remove(channel);
        return "VERY_CUTE_MAU_HELLO_KITTY;" + game.getOwnerName();
    }

    public void awaitAuthentication(SelectableChannel channel, Game game) {
        awaitingAuthentication.put(channel, game);
    }

    private String askForGameJoin(SelectableChannel channel, Game game, String message) {
        String[] messageParams = message.split(";");
        String hostName = messageParams[1];
        SelectableChannel hostChannel = hostedGames.keySet().stream()
                .filter(c -> hostedGames.get(c).getOwnerName().equals(hostName))
                .findAny()
                .orElse(null);
        if (hostChannel == null) {
            return "NO_SUCH_HOSTING_KITTY;" + hostName;
        }
        Game guestGame = waitingForGames.get(channel);
        if (guestGame == null || guestGame.getPlayerState() != PlayerState.AWAITS_GAME) {
            return "BAD_FUR_STATE_FOR_JOINING_GAME_NAUGHTY_KITTY;" + (guestGame != null ? guestGame.getPlayerState().name() : "DISCONNECTED");
        }
        try {
            final ByteBuffer buffer = ByteBuffer.wrap((InputMessages.INVITATION + ";" + guestGame.getOwnerName() + "\n").getBytes());
            doWrite(buffer, (SocketChannel) hostChannel);
            Game hostGame = hostedGames.get(hostChannel);
            hostGame.getInvitations().add(Invitation.builder()
                        .creationTime(new Date().getTime())
                        .invitator(hostGame.getOwnerName())
                        .invitatorChannel(hostChannel)
                        .invitedPlayer(guestGame.getOwnerName())
                        .invitedPlayerChannel(channel)
                        .build());
            guestGame.setPlayerState(PlayerState.AWAITING_INVITATION_DECISION);
            return "INVITATION_SENT";
        } catch (Exception e){
            log.error("Error while invitation request to host", e);
            return "INVITATION_NOT_SENT";
        }
    }

    private String createInvitationAndInvitePlayer(SelectableChannel channel, String invitedPlayer) {
        final Game game = hostedGames.get(channel);
        Invitation invitation = game.getInvitations().stream()
                .filter(i -> i.getInvitedPlayer().equals(invitedPlayer))
                .findFirst()
                .orElse(null);
        if (invitation != null) {
            if (waitingForGames.containsKey(invitation.getInvitedPlayerChannel())) {
                return "ALREADY_INVITED;" + invitedPlayer;
            } else {
                game.getInvitations().remove(invitation);
            }
        }
        final SelectableChannel invitedPlayerChannel = waitingForGames.keySet().stream()
                .filter(k -> waitingForGames.get(k).getOwnerName().equals(invitedPlayer))
                .findFirst()
                .orElse(null);
        if (invitedPlayerChannel == null) {
            return "PLAYER_UNAVAILABLE;" + invitedPlayer;
        }
        game.getInvitations().add(
                Invitation.builder()
                    .creationTime(new Date().getTime())
                    .invitator(game.getOwnerName())
                    .invitedPlayer(invitedPlayer)
                    .invitatorChannel(channel)
                    .invitedPlayerChannel(invitedPlayerChannel)
                    .build());
        final ByteBuffer response = ByteBuffer.wrap((InputMessages.INVITATION + ";" + game.getOwnerName()
            + Constants.END_MESSAGE_MARKER + "\n").getBytes());
        try {
            doWrite(response, (SocketChannel) invitedPlayerChannel);
            //waitingForGames.get(invitedPlayerChannel).setPlayerState(PlayerState.AWAITING_INVITATION_DECISION);
        }  catch (IOException e) {
            log.error("Error while sending invitation message from " + game.getOwnerName() + " to " + invitedPlayer, e);
        }
        return "INVITATION_SENT";
    }

    public String awaitGame(SelectableChannel channel, Game game, String message) {
        if (game.getPlayerState() != PlayerState.AUTHENTICATED) {
            return "BAD_CAT_STATE_FOR_AWAITING_GAME";
        }
        if (waitingForGames.get(channel) != null) {
            return "CUTE_KITTY_YOU_ARE_ALREADY_WAITING_FOR_A_GAME";
        }
        game.setPlayerState(PlayerState.AWAITS_GAME);
        waitingForGames.put(channel, game);
        return "AWAITING_FOR_GAME;" + game.getOwnerName();
    }

    private void preparePlayersResponse(Map<SelectableChannel, Game> games, StringBuilder response, Game game) {
        games.values()
                .stream()
                .filter(g -> !g.getOwnerName().equals(game.getOwnerName()))
                .forEach(g -> {
                    response.append(g.getOwnerName());
                    response.append(";");
                    return;
                });
    }

    public void checkIfSendHeathBeatMessageAndSendItIfIsNeeded(SelectableChannel channel, long currentTime,
                                                               Game game) throws IOException {
        if (currentTime - game.getLastHearthBeatTime() > DISCONNECTION_CHECK_INTERVAL) {
            final ByteBuffer buffer = ByteBuffer.wrap((InputMessages.HEARTHBEAT + "\n").getBytes());
            doWrite(buffer, (SocketChannel) channel);
            game.setLastHearthBeatTime(currentTime);
        }
    }

    private void sentNewGameCreatedMessage(SelectableChannel channel, Game game, String userName) {
        try {
            final ByteBuffer buffer = ByteBuffer.wrap(("NEW_GAME_CREATED;" + userName + "\n").getBytes());
            doWrite(buffer, (SocketChannel) channel);
        } catch (Exception e){
            log.error("Error while sending new game created message", e);
        }
    }


}
