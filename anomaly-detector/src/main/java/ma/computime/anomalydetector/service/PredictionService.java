// Emplacement : src/main/java/ma/computime/anomalydetector/service/PredictionService.java
package ma.computime.anomalydetector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient; // <-- On utilise le nouveau RestClient
// import org.springframework.web.client.RestTemplate; // <-- L'ancien import est supprimé

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
public class PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);

    // CHANGEMENT 1: On remplace RestTemplate par RestClient.
    // 'final' signifie qu'il doit être initialisé une fois et ne plus changer.
    private final RestClient restClient;

    // CHANGEMENT 2: On utilise l'injection par constructeur.
    // C'est la meilleure pratique. Spring va automatiquement fournir un "RestClient.Builder".
    public PredictionService(RestClient.Builder restClientBuilder) {
        // On configure l'URL de base ici, une seule fois.
        this.restClient = restClientBuilder
                .baseUrl("http://localhost:5000")
                .build();
    }

    /**
     * Appelle le service externe d'IA pour obtenir une prédiction de pointage manquant.
     * Cette méthode utilise maintenant RestClient, qui est plus moderne et lisible.
     * @param dateAnomalie La date de l'omission.
     * @param badge Le badge de l'employé concerné.
     * @return Un Optional contenant l'heure prédite, ou un Optional vide en cas d'erreur.
     */
    public Optional<LocalTime> getPrediction(LocalDate dateAnomalie, String badge) {
        
        // La création du corps de la requête ne change pas.
        Map<String, Object> requestBody = Map.of(
            "date", dateAnomalie.toString(),
            "badge", badge
        );

        try {
            logger.info("Appel de l'API de prédiction (via RestClient) pour le badge {} à la date {}.", badge, dateAnomalie);

            // CHANGEMENT 3: La syntaxe de l'appel est plus "fluide".
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
            // La gestion des erreurs reste la même, c'est très important.
            logger.error("Erreur de communication avec le service de prédiction IA. Message: {}", e.getMessage());
            return Optional.empty();
        }
    }
}