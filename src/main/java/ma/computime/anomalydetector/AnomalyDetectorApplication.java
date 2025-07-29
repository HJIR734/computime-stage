// Emplacement : src/main/java/ma/computime/anomalydetector/AnomalyDetectorApplication.java

package ma.computime.anomalydetector;

// Imports de Spring Boot
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient; 
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value; // Cet import est nécessaire

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class AnomalyDetectorApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println("--- Application running in UTC timezone ---");
    }
    
    public static void main(String[] args) {
        SpringApplication.run(AnomalyDetectorApplication.class, args);
    }

    // ======================================================================================
    // === C'EST CETTE MÉTHODE QUE TU DOIS REMPLACER ===
    // ======================================================================================

    /**
     * Crée un bean RestClient moderne qui lit l'URL de base depuis application.properties.
     * C'est une meilleure pratique car cela rend l'URL configurable sans recompiler.
     *
     * @param baseUrl La valeur de la propriété 'ia.api.base-url' injectée par Spring.
     * @return une instance configurée de RestClient.
     */
    @Bean
    public RestClient restClient(@Value("${ia.api.base-url}") String baseUrl) {
        System.out.println("--- Configuration du RestClient pour communiquer avec l'API IA sur l'URL : " + baseUrl + " ---");
        return RestClient.builder()
                .baseUrl(baseUrl) // Utilise la variable au lieu d'une valeur en dur
                .build();
    }
    
    // ======================================================================================
    // === La méthode restTemplate() reste inchangée ===
    // ======================================================================================
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}