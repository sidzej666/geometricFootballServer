package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.Builder;
import lombok.Getter;

import java.nio.channels.SelectableChannel;

@Builder
@Getter
public class Invitation {
    private final String invitator;
    private final SelectableChannel invitatorChannel;
    private final String invitedPlayer;
    private final SelectableChannel invitedPlayerChannel;
    private final long creationTime;
}
