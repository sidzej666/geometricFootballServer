package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderPosition {
    float x;
    float y;
    List<Order> orders;
}
