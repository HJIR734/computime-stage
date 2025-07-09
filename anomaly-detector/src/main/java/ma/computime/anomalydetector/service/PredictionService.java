// Emplacement : src/main/java/ma/computime/anomalydetector/service/PredictionService.java
package ma.computime.anomalydetector.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PredictionService {

    // 1. Déclaration du Logger pour cette classe.
    // C'est l'outil standard pour écrire des messages dans la console.
    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PREDICTION_URL = "http://localhost:5000/predict";

    /**
     * Appelle le service externe d'IA pour obtenir une prédiction de pointage manquant.
     * Cette méthode est maintenant ROBUSTE : elle gère les pannes du service IA.
     * @param dateAnomalie La date de l'omission.
     * @param badge Le badge de l'employé concerné.
     * @return Un Optional contenant l'heure prédite, ou un Optional vide en cas d'erreur.
     */
    public Optional<LocalTime> getPrediction(LocalDate dateAnomalie, String badge) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("date", dateAnomalie.toString());
        requestBody.put("badge", badge); // On envoie le badge pour la prédiction personnalisée

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            // 2. L'appel réseau est maintenant dans un bloc try-catch.
            logger.info("Appel de l'API de prédiction pour le badge {} à la date {}.", badge, dateAnomalie);
            ResponseEntity<Map> response = restTemplate.postForEntity(PREDICTION_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String predictionStr = (String) response.getBody().get("prediction");
                logger.info("Prédiction reçue de l'IA pour le badge {}: {}", badge, predictionStr);
                // On s'attend à un format comme "09:46", donc on ajoute les secondes pour le parsing.
                if (predictionStr.length() == 5) { 
                    predictionStr += ":00";
                }
                return Optional.of(LocalTime.parse(predictionStr, DateTimeFormatter.ofPattern("HH:mm:ss")));
            } else {
                logger.warn("L'API de prédiction a répondu avec un statut d'erreur : {}", response.getStatusCode());
                return Optional.empty();
            }

        } catch (RestClientException e) {
            // 3. Si le service IA ne répond pas (éteint, erreur réseau...), on "attrape" l'exception ici.
            // L'application ne plantera pas.
            logger.error("Erreur de communication avec le service de prédiction IA. Le service est-il démarré ? Message: {}", e.getMessage());
            return Optional.empty(); // On retourne un résultat vide au lieu de planter.
        }
    }
}