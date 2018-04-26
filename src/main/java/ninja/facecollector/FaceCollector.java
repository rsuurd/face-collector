package ninja.facecollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class FaceCollector {
	public static void main(String... parameters) {
		SpringApplication.run(FaceCollector.class, parameters);
	}

	@Bean
	public RestOperations restOperations() {
		return new RestTemplate();
	}
}
