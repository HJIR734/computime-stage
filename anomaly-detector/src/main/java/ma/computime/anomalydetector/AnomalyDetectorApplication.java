// Emplacement : src/main/java/ma/computime/anomalydetector/AnomalyDetectorApplication.java

package ma.computime.anomalydetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AnomalyDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnomalyDetectorApplication.class, args);
	}

	/**
	 * Crée un bean RestTemplate qui sera géré par le conteneur Spring.
	 * Cela nous permet d'injecter et d'utiliser RestTemplate n'importe où
	 * dans notre application (par exemple, dans nos services) pour faire des appels
	 * à d'autres APIs REST, comme notre service Python.
	 *
	 * @return une instance de RestTemplate.
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}