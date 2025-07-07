// Emplacement : ma/computime/anomalydetector/service/AnomalieDetectionService.java
package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.dto.PredictionRequest;
import ma.computime.anomalydetector.dto.PredictionResponse;
import ma.computime.anomalydetector.entity.*;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnomalieDetectionService {

    @Autowired private EmployeRepository employeRepository;
    @Autowired private PointageRepository pointageRepository;
    @Autowired private AnomalieRepository anomalieRepository;
    @Autowired private RestTemplate restTemplate;

    private static final String PREDICTION_API_URL = "http://localhost:5000/predict/entree";

    public void lancerDetectionPourTous(LocalDate jour) {
        List<Employe> employes = employeRepository.findAll();
        for (Employe employe : employes) {
            List<Pointage> pointagesDuJour = pointageRepository.findByBadgeEmploye(employe.getBadge())
                    .stream()
                    .filter(p -> p.getDateMouvement().toLocalDate().equals(jour))
                    .sorted(Comparator.comparing(Pointage::getDateMouvement))
                    .collect(Collectors.toList());

            if (pointagesDuJour.isEmpty()) {
                continue;
            }

            detecterOmission(employe, jour, pointagesDuJour);

            if (pointagesDuJour.size() % 2 == 0) {
                Planning planning = employe.getPlanning();
                if (planning == null) continue;

                Optional<Horaire> horaireDuJourOpt = planning.getHorairePourJour(jour.getDayOfWeek());
                if (horaireDuJourOpt.isPresent()) {
                    Horaire horaireDuJour = horaireDuJourOpt.get();
                    detecterRetard(employe, jour, pointagesDuJour, horaireDuJour);
                    detecterHeureSupplementaire(employe, jour, pointagesDuJour, horaireDuJour);
                }
            }
        }
    }

    private void detecterOmission(Employe employe, LocalDate jour, List<Pointage> pointages) {
        if (pointages.size() % 2 != 0) {
            String message = "Nombre de pointages impair détecté (" + pointages.size() + " pointages).";
            String suggestionTexte = "Vérifier manuellement le pointage manquant.";
            LocalTime suggestionHeure = null;
            
            try {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                PredictionRequest requestBody = new PredictionRequest(
                        jour.getDayOfWeek().getValue(),
                        jour.getDayOfMonth(),
                        jour.getMonthValue(),
                        jour.get(weekFields.weekOfWeekBasedYear())
                );

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<PredictionRequest> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<PredictionResponse> responseEntity = restTemplate.exchange(
                    PREDICTION_API_URL, HttpMethod.POST, requestEntity, PredictionResponse.class);

                if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
                    PredictionResponse response = responseEntity.getBody();
                    if (response.getSuggestionHeure() != null) {
                        suggestionTexte = "Suggestion IA: " + response.getSuggestionHeure();
                        try {
                            suggestionHeure = LocalTime.parse(response.getSuggestionHeure());
                        } catch (Exception parseException) {
                            System.err.println("Impossible de parser l'heure : " + response.getSuggestionHeure());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("--- ERREUR LORS DE L'APPEL A L'API PYTHON pour le badge " + employe.getBadge() + " ---");
                e.printStackTrace();
                System.err.println("--------------------------------------------------------------------");
            }

            creerEtSauverAnomalie(employe, jour, TypeAnomalie.OMISSION_POINTAGE, message, suggestionTexte, suggestionHeure);
        }
    }

    private void detecterRetard(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire) {
        LocalTime heureDebutTheorique = horaire.getHeureDebutTheorique();
        if (heureDebutTheorique == null) return;
        
        Pointage premierPointage = pointages.get(0);
        if (premierPointage.getDateMouvement().toLocalTime().isAfter(heureDebutTheorique)) {
            long minutes = Duration.between(heureDebutTheorique, premierPointage.getDateMouvement().toLocalTime()).toMinutes();
            if (minutes > 0) {
                String message = "Arrivée à " + premierPointage.getDateMouvement().toLocalTime().withNano(0) +
                                 " au lieu de " + heureDebutTheorique + " (retard de " + minutes + " minutes).";
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.RETARD, message, "Justificatif de retard requis.", null);
            }
        }
    }

    private void detecterHeureSupplementaire(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire) {
        LocalTime heureFinTheorique = horaire.getHeureFinTheorique();
        if (heureFinTheorique == null) return;

        Pointage dernierPointage = pointages.get(pointages.size() - 1);
        if (dernierPointage.getDateMouvement().toLocalTime().isAfter(heureFinTheorique)) {
            long minutes = Duration.between(heureFinTheorique, dernierPointage.getDateMouvement().toLocalTime()).toMinutes();
            if (minutes > 0) {
                String message = "Sortie à " + dernierPointage.getDateMouvement().toLocalTime().withNano(0) +
                                 " alors que la journée se termine à " + heureFinTheorique + ".";
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.HEURE_SUP_NON_AUTORISEE, message, "Valider ou rejeter les heures supplémentaires.", null);
            }
        }
    }
    
    // Cette méthode a une signature "void" et ne retourne rien. C'est plus simple.
    private void creerEtSauverAnomalie(Employe employe, LocalDate jour, TypeAnomalie type, String message, String suggestionTexte, LocalTime valeurSuggestion) {
        if (anomalieRepository.existsByEmployeAndJourAnomalieAndTypeAnomalie(employe, jour, type)) {
            return; // On ne fait rien si une anomalie du même type existe déjà pour ce jour.
        }

        Anomalie anomalie = new Anomalie();
        anomalie.setEmploye(employe);
        anomalie.setJourAnomalie(jour);
        anomalie.setTypeAnomalie(type);
        anomalie.setMessage(message);
        anomalie.setSuggestion(suggestionTexte);
        anomalie.setValeurSuggestion(valeurSuggestion);
        anomalie.setStatut(StatutAnomalie.EN_ATTENTE);
        
        anomalieRepository.save(anomalie);
    }
}