package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer;

import catAndDogStudio.geometricfootballserver.infrastructure.Constants;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.model.JsonFootballer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerObjectFactory {
    private final ObjectMapper objectMapper;
    public List<JsonFootballer> construct(final String teamPlayers) {
        List<JsonFootballer> jsonFootballers = new ArrayList<>();
        String[] stringFootballers = teamPlayers.split(Constants.MESSAGE_SEPARATOR);
        for (final String stringFootballer : stringFootballers) {
            try {
                final JsonFootballer jsonFootballer = objectMapper.readValue(stringFootballer, JsonFootballer.class);
                jsonFootballers.add(jsonFootballer);
            } catch (final IOException e) {
                log.warn("exception trying to parse team players", e);
            }
        }
        return jsonFootballers;
    }
}
