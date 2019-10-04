package catAndDogStudio.geometricfootballserver.infrastructure;

import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.model.JsonFootballer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Footballer {
    String uniqueId;
    List<OrderPosition> orders;
    JsonFootballer jsonFootballer;
}
