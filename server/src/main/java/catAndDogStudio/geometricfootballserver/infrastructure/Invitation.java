package catAndDogStudio.geometricfootballserver.infrastructure;

import io.netty.channel.Channel;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Invitation {
    private final String invitator;
    private final Channel invitatorChannel;
    private final String invitedPlayer;
    private final Channel invitedPlayerChannel;
    private final long creationTime;
    private final String preferredColor;
}
