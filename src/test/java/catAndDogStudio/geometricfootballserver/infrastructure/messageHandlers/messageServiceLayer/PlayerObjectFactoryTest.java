package catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer;

import catAndDogStudio.geometricfootballserver.GeometricFootballServerConfiguration;
import catAndDogStudio.geometricfootballserver.infrastructure.messageHandlers.messageServiceLayer.model.JsonFootballer;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@Import({GeometricFootballServerConfiguration.class, PlayerObjectFactory.class})
public class PlayerObjectFactoryTest {

    @Autowired
    private PlayerObjectFactory playerObjectFactory;

    @Test
    public void shouldParseJsonFootballers() {
        String footballers = "{\"uniqueId\":\"50\",\"dribbling\":\"10\"}";
        List<JsonFootballer> jsonFootballerList = playerObjectFactory.construct(footballers);
        Assertions.assertThat(jsonFootballerList.get(0).getUniqueId()).isEqualTo("50");
    }
}