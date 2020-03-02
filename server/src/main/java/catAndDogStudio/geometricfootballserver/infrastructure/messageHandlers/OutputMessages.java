package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.Game;
import catAndDogStudio.geometricfootballserver.infrastructure.PlayerState;

public class OutputMessages {
    public final static String PLAYERS = "PLAYERS";
    public final static String ALREADY_INVITED = "ALREADY_INVITED";
    public final static String PLAYER_UNAVAILABLE = "PLAYER_UNAVAILABLE";
    public final static String INVITATION = "INVITATION";
    public final static String INVITATION_SENT = "INVITATION_SENT";
    public final static String VERY_CUTE_MAU_HELLO_KITTY = "VERY_CUTE_MAU_HELLO_KITTY";
    public final static String YOU_ARE_MAUING_IN_A_VERY_STRANGE_WAY_KITTY = "YOU_ARE_MAUING_IN_A_VERY_STRANGE_WAY_KITTY";
    public final static String CUTE_KITTY_YOU_ARE_ALREADY_HOSTING_A_GAME = "CUTE_KITTY_YOU_ARE_ALREADY_HOSTING_A_GAME";
    public final static String GAME_HOSTED = "GAME_HOSTED";
    public final static String CUTE_KITTY_YOU_ARE_ALREADY_WAITING_FOR_A_GAME = "CUTE_KITTY_YOU_ARE_ALREADY_WAITING_FOR_A_GAME";
    public final static String AWAITING_FOR_GAME = "AWAITING_FOR_GAME";
    public final static String HOST_LEFT = "HOST_LEFT";
    public final static String LEFT_FROM_GAME = "LEFT_FROM_GAME";
    public final static String PLAYER_LEFT = "PLAYER_LEFT";
    public final static String NO_SUCH_HOSTING_KITTY = "NO_SUCH_HOSTING_KITTY";
    public final static String INVITATION_NOT_FOUND = "INVITATION_NOT_FOUND";
    public final static String INVITATION_CANCELLED = "INVITATION_CANCELLED";
    public final static String KITTY_JOINED_GAME = "KITTY_JOINED_GAME";
    public final static String TEAM_PLAYERS = "TEAM_PLAYERS";
    public final static String OPPONENT_TEAM_PLAYERS = "OPPONENT_TEAM_PLAYERS";
    public final static String KITTY_INVITATION_REJECTED = "KITTY_INVITATION_REJECTED";
    public final static String KITTY_NOT_WANTED = "KITTY_NOT_WANTED";
    public final static String INVITATION_ACCEPTED_BY_GUEST = "INVITATION_ACCEPTED_BY_GUEST";
    public final static String INVITATION_REJECTED_BY_GUEST = "INVITATION_REJECTED_BY_GUEST";
    public final static String NO_SUCH_KITTY_IN_GAME_KUWETA = "NO_SUCH_KITTY_IN_GAME_KUWETA";
    public final static String KITTY_KICKED_OUT = "KITTY_KICKED_OUT";
    public final static String PONG = "PONG";
    public final static String TEAM_TACTIC = "TEAM_TACTIC";
    public final static String TEAM_TEAM = "TEAM_TEAM";
    public final static String TEAM_TEAM_PLAYERS = "TEAM_TEAM_PLAYERS";
    public final static String TEAM_TACTIC_MAPPING = "TEAM_TACTIC_MAPPING";
    public final static String TEAM_PLAYERS_USERS_MAPPING = "TEAM_PLAYERS_USERS_MAPPING";
    public final static String READY_FOR_GAME_VALIDATION_ERROR = "READY_FOR_GAME_VALIDATION_ERROR";
    public final static String READY_FOR_GAME_TRANSITION = "READY_FOR_GAME_TRANSITION";
    public final static String GO_BACK_TO_TEAM_PREPARATION_TRANSITION = "GO_BACK_TO_TEAM_PREPARATION_TRANSITION";
    public final static String INVITATION_NOT_ALLOWED = "INVITATION_NOT_ALLOWED";
    public final static String OPPONENT_TEAM_TACTIC = "OPPONENT_TEAM_TACTIC";
    public final static String OPPONENT_TEAM_TEAM = "OPPONENT_TEAM_TEAM";
    public final static String OPPONENT_TEAM_TEAM_PLAYERS = "OPPONENT_TEAM_TEAM_PLAYERS";
    public final static String OPPONENT_TEAM_TACTIC_MAPPING = "OPPONENT_TEAM_TACTIC_MAPPING";
    public final static String OPPONENT_TEAM_PLAYERS_USERS_MAPPING = "OPPONENT_TEAM_PLAYERS_USERS_MAPPING";
    public final static String OPPONENT_FOUND = "OPPONENT_FOUND";

    public final static String getReadyForGameTransitionMessage() {
        return READY_FOR_GAME_TRANSITION;
    }
    public final static String getInvitationNotAllowedMessage(String hostName, PlayerState hostState) {
        return INVITATION_NOT_ALLOWED + Constants.MESSAGE_SEPARATOR + "Host " + hostName + " is already in "
                + hostState + " state";
    }
    public final static String getGoBackToTeamPreparationTransition() {
        return GO_BACK_TO_TEAM_PREPARATION_TRANSITION;
    }
    public final static String getOpponentFoundMessage(final String opponentHostName) {
        return OPPONENT_FOUND + Constants.MESSAGE_SEPARATOR + opponentHostName;
    }
}
