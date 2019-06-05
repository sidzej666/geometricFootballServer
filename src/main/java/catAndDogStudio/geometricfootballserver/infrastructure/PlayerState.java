package catAndDogStudio.geometricfootballserver.infrastructure;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PlayerState {
    AWAITING_AUTHENTICATION(

    ),
    AUTHENTICATED(
            Stream.of(AWAITING_AUTHENTICATION).collect(Collectors.toSet())
    ),
    GAME_HOST(
            Stream.of(AUTHENTICATED).collect(Collectors.toSet())
    ),
    AWAITS_GAME(
            Stream.of(AUTHENTICATED).collect(Collectors.toSet())
    ),
    AWAITING_INVITATION_DECISION(
            Stream.of(AWAITS_GAME).collect(Collectors.toSet())
    ),
    BLOCK_NEW_INVITATIONS(
            Stream.of(GAME_HOST, AWAITS_GAME).collect(Collectors.toSet())
    ),
    GAME_GUEST(
            Stream.of(AWAITING_INVITATION_DECISION).collect(Collectors.toSet())
    ),
    IN_GAME(
            Stream.of(AWAITING_INVITATION_DECISION).collect(Collectors.toSet())
    ),
    WAIT_FOR_OPPONENTS(
            Stream.of(GAME_HOST, IN_GAME).collect(Collectors.toSet())
    );

    public final Set<PlayerState> possibleStatesForTransition;

    private PlayerState(final Set<PlayerState> possibleStatesForTransition) {
        this.possibleStatesForTransition = possibleStatesForTransition;
    }
    private PlayerState() {
        this.possibleStatesForTransition = null;
    }
}
