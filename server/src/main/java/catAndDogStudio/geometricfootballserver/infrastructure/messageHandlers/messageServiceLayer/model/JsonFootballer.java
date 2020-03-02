package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Value;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonFootballer {
    String uniqueId;
}
