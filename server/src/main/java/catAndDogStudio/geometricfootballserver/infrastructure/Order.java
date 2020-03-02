package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
    private String orderId;
    private String orderType;
    private String orderParams;
}
