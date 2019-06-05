package catAndDogStudio.geometricfootballserver;

import catAndDogStudio.geometricfootballserver.infrastructure.DogServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class GeometricFootballServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeometricFootballServerApplication.class, args);
	}
	/*
	public static void main(String[] args) {
		DogServer.builder().port(8011).build().start();
	}
	*/
}
