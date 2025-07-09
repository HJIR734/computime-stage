// Emplacement : ma/computime/anomalydetector/service/AnomalieDetectionService.java
package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.dto.IaSuggestionResponse;
import ma.computime.anomalydetector.dto.PredictionResponse;
import ma.computime.anomalydetector.entity.*;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.JourFerieRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient; // On n'utilise plus que RestClient

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnomalieDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(AnomalieDetectionService.class);

    @Autowired private EmployeRepository employeRepository;
    @Autowired private PointageRepository pointageRepository;
    @Autowired private AnomalieRepository anomalieRepository;
    @Autowired private JourFerieRepository jourFerieRepository;
    
    // On n'a plus besoin de RestTemplate, on le supprime
    // @Autowired private RestTemplate restTemplate; 
    @Autowired private RestClient restClient; // On utilise uniquement le RestClient

    public void lancerDetectionPourTous(LocalDate jour) {
        logger.info("Lancement de la détection des anomalies pour le jour : {}", jour);
        List<Employe> employes = employeRepository.findAll();
        for (Employe employe : employes) {
            List<Pointage> pointagesDuJour = pointageRepository.findByBadgeEmploye(employe.getBadge())
                    .stream()
                    .filter(p -> p.getDateMouvement().toLocalDate().equals(jour))
                    .sorted(Comparator.comparing(Pointage::getDateMouvement))
                    .collect(Collectors.toList());

            if (!pointagesDuJour.isEmpty()) {
                boolean anomalieJourNonTravailleTrouvee = detecterTravailJourNonTravaille(employe, jour);
                if (anomalieJourNonTravailleTrouvee) {
                    continue;
                }
            } else {
                continue;
            }

            boolean aUneOmission = pointagesDuJour.size() % 2 != 0;

            if (aUneOmission) {
                // APPEL À LA MÉTHODE CORRIGÉE
                detecterOmissionAvecIA(employe, jour, pointagesDuJour);
            } else {
                Planning planning = employe.getPlanning();
                if (planning == null) {
                    logger.warn("L'employé avec badge {} n'a pas de planning affecté. Impossible de détecter les retards/HS.", employe.getBadge());
                    continue;
                }
                Optional<Horaire> horaireDuJourOpt = planning.getHorairePourJour(jour.getDayOfWeek());
                if (horaireDuJourOpt.isPresent()) {
                    Horaire horaireDuJour = horaireDuJourOpt.get();
                    // On peut ajouter la détection de retard ici si on veut
                    // detecterRetard(employe, jour, pointagesDuJour, horaireDuJour);
                    detecterHeureSupplementaireAvecIA(employe, jour, pointagesDuJour, horaireDuJour);
                }
            }
        }
        logger.info("Détection des anomalies terminée pour le jour : {}", jour);
    }
    
    // =========================================================================
    // === MÉTHODE D'OMISSION CORRIGÉE POUR UTILISER RESTCLIENT ET MAP =========
    // =========================================================================
    private void detecterOmissionAvecIA(Employe employe, LocalDate jour, List<Pointage> pointages) {
        String message = "Nombre de pointages impair détecté (" + pointages.size() + " pointages).";
        String suggestionTexte = "Validation manuelle requise.";
        LocalTime suggestionHeure = null;

        try {
            logger.info("Tentative d'appel à l'IA pour l'omission du badge {} le {}", employe.getBadge(), jour);
            
            // On utilise une Map pour être sûr que les noms des clés JSON sont corrects
            Map<String, Object> requestBody = new HashMap<>();
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            
            requestBody.put("jour_de_semaine", jour.getDayOfWeek().getValue()); // 1 pour Lundi, 7 pour Dimanche
            requestBody.put("jour_du_mois", jour.getDayOfMonth());
            requestBody.put("mois", jour.getMonthValue());
            requestBody.put("semaine_de_annee", jour.get(weekFields.weekOfWeekBasedYear()));
            requestBody.put("badge", employe.getBadge());
            
            // On utilise le RestClient (comme pour les heures sup)
            PredictionResponse response = restClient.post()
                    .uri("/predict/entree") // L'URL est correcte
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(PredictionResponse.class);

            if (response != null && response.getSuggestionHeure() != null && !response.getSuggestionHeure().isEmpty()) {
                suggestionTexte = "Suggestion IA: " + response.getSuggestionHeure();
                suggestionHeure = LocalTime.parse(response.getSuggestionHeure());
                logger.info("Suggestion de l'IA reçue pour l'omission du badge {}: {}", employe.getBadge(), suggestionTexte);
            }
        } catch (Exception e) {
            logger.error("Erreur de communication avec le service de prédiction IA pour l'omission. Message: {}", e.getMessage());
            suggestionTexte = "Suggestion non disponible (IA injoignable)";
        }
        
        creerEtSauverAnomalie(employe, jour, TypeAnomalie.OMISSION_POINTAGE, message, suggestionTexte, suggestionHeure);
    }


    // Le reste du code ne change pas
    private boolean detecterTravailJourNonTravaille(Employe employe, LocalDate jour) {
        if (jourFerieRepository.existsByDate(jour)) {
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.TRAVAIL_JOUR_FERIE, "Pointages détectés un jour férié (" + jour + ").", "Vérifier compensation.", null);
            return true;
        }
        Planning planning = employe.getPlanning();
        if (planning != null && planning.getHorairePourJour(jour.getDayOfWeek()).isEmpty()) {
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.TRAVAIL_JOUR_REPOS, "Pointages détectés un jour de repos.", "Vérifier compensation.", null);
            return true;
        }
        return false;
    }

    private void detecterHeureSupplementaireAvecIA(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire) {
        if (pointages.size() < 2) return;
        Pointage premierPointage = pointages.get(0);
        Pointage dernierPointage = pointages.get(pointages.size() - 1);
        long dureeTravailleeSecondes = Duration.between(premierPointage.getDateMouvement(), dernierPointage.getDateMouvement()).toSeconds();
        long dureeTheoriqueSecondes = Duration.between(horaire.getHeureDebutTheorique(), horaire.getHeureFinTheorique()).getSeconds();

        if (dureeTravailleeSecondes > dureeTheoriqueSecondes) {
            long hsSecondes = dureeTravailleeSecondes - dureeTheoriqueSecondes;
            if (hsSecondes / 60 > 0) {
                logger.info("Heure supplémentaire détectée pour le badge {}. Appel de l'IA...", employe.getBadge());
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("badge", employe.getBadge());
                requestBody.put("jour_semaine", jour.getDayOfWeek().getValue() - 1);
                requestBody.put("hs_secondes", hsSecondes);
                String suggestionTexte = "Validation manuelle requise.";
                try {
                    IaSuggestionResponse suggestion = restClient.post().uri("/predict/overtime").contentType(MediaType.APPLICATION_JSON).body(requestBody).retrieve().body(IaSuggestionResponse.class);
                    if (suggestion != null) {
                        suggestionTexte = String.format("Suggestion IA : %s (Confiance : %s)", suggestion.getDecision(), suggestion.getConfiance());
                        logger.info("Suggestion de l'IA reçue pour l'heure sup du badge {}: {}", employe.getBadge(), suggestionTexte);
                    }
                } catch (Exception e) {
                    logger.error("Erreur de communication avec le service de prédiction HS de l'IA. Message: {}", e.getMessage());
                    suggestionTexte = "Suggestion non disponible (IA injoignable)";
                }
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.HEURE_SUP_NON_AUTORISEE, String.format("Heure supplémentaire de %d minutes détectée.", hsSecondes / 60), suggestionTexte, null);
            }
        }
    }

    private void creerEtSauverAnomalie(Employe employe, LocalDate jour, TypeAnomalie type, String message, String suggestionTexte, LocalTime valeurSuggestion) {
        if (anomalieRepository.existsByEmployeAndJourAnomalieAndTypeAnomalie(employe, jour, type)) {
            logger.warn("Une anomalie de type {} existe déjà pour l'employé {} le {}. Ignorée.", type, employe.getBadge(), jour);
            return;
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
        logger.info("Anomalie de type {} créée pour l'employé {}.", type, employe.getBadge());
    }
}