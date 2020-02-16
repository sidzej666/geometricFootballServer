package catAndDogStudio.geometricfootballserver.clients;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

@Data
@JsonDeserialize(builder = Message.MessageBuilder.class)
@Builder(builderClassName = "MessageBuilder", toBuilder = true)
public class Message {
    byte[] message;

    @JsonPOJOBuilder(withPrefix = "")
    public static class MessageBuilder {
    }
}
