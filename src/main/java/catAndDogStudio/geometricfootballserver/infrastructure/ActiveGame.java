package catAndDogStudio.geometricfootballserver.infrastructure;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ActiveGame {
    // main key - game owner
    // inner map key - footballer owner
    private Map<String, Map<String, Footballer>> footballers = new HashMap<>();
}
