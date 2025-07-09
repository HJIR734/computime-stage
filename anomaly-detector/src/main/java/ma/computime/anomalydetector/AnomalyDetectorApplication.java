// Emplacement : src/main/java/ma/computime/anomalydetector/AnomalyDetectorApplication.java

package ma.computime.anomalydetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient; 
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AnomalyDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnomalyDetectorApplication.class, args);
    }

    /**
     * Crée un bean RestClient moderne pour faire des appels HTTP vers un service externe.
     * Ce client sera injecté automatiquement dans les services qui en ont besoin.
     * On peut l'utiliser pour envoyer des requêtes vers le serveur Python d'IA, par exemple.
     *
     * @return une instance configurée de RestClient.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:5000") 
                .build();
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
