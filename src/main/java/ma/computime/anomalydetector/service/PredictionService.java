// Emplacement : src/main/java/ma/computime/anomalydetector/service/PredictionService.java
package ma.computime.anomalydetector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient; 
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
public class PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);

    
    private final RestClient restClient;

    
    public PredictionService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:5000")
                .build();
    }

    
    public Optional<LocalTime> getPrediction(LocalDate dateAnomalie, String badge) {
        
        Map<String, Object> requestBody = Map.of(
            "date", dateAnomalie.toString(),
            "badge", badge
        );

        try {
            logger.info("Appel de l'API de prédiction (via RestClient) pour le badge {} à la date {}.", badge, dateAnomalie);

            Map responseBody = this.restClient.post() // On commence un appel POST
                    .uri("/predict")                 // On ajoute le chemin de l'endpoint
                    .contentType(MediaType.APPLICATION_JSON) // On définit le type de contenu
                    .body(requestBody)               // On attache le corps de la requête
                    .retrieve()                      // On exécute l'appel et on récupère la réponse
                    .body(Map.class);                // On convertit le corps de la réponse en Map

            if (responseBody != null && responseBody.get("prediction") != null) {
                String predictionStr = (String) responseBody.get("prediction");
                logger.info("Prédiction reçue de l'IA pour le badge {}: {}", badge, predictionStr);
                
                if (predictionStr.length() == 5) { 
                    predictionStr += ":00";
                }
                return Optional.of(LocalTime.parse(predictionStr, DateTimeFormatter.ofPattern("HH:mm:ss")));
            
            } else {
                logger.warn("L'API de prédiction a répondu mais le corps de la réponse est vide ou invalide.");
                return Optional.empty();
            }

        } catch (Exception e) {
            
            logger.error("Erreur de communication avec le service de prédiction IA. Message: {}", e.getMessage());
            return Optional.empty();
        }
    }
}