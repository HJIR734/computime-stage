// Emplacement : src/main/java/ma/computime/anomalydetector/service/AnomalieDetectionService.java
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
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
    @Autowired private RestClient restClient;

    public void lancerDetectionPourTous(LocalDate jour) {
        logger.info("Lancement de la détection des anomalies pour le jour : {}", jour);
        List<Employe> employes = employeRepository.findAll();

        for (Employe employe : employes) {
            List<Pointage> pointagesDuJour = pointageRepository.findByBadgeEmployeAndDateMouvementBetween(
                    employe.getBadge(),
                    jour.atStartOfDay(),
                    jour.plusDays(1).atStartOfDay()
            ).stream()
             .sorted(Comparator.comparing(Pointage::getDateMouvement))
             .collect(Collectors.toList());

            if (pointagesDuJour.isEmpty()) {
                detecterAbsenceInjustifiee(employe, jour);
                continue;
            }
            
            boolean estUnJourOff = detecterTravailJourNonTravaille(employe, jour);
            if (estUnJourOff) {
                continue;
            }

            Planning planning = employe.getPlanning();
            if (planning == null) {
                logger.warn("L'employé avec badge {} n'a pas de planning affecté. Impossible de détecter les retards/HS.", employe.getBadge());
                continue;
            }

            Optional<Horaire> horaireDuJourOpt = planning.getHorairePourJour(jour.getDayOfWeek());

            boolean aUneOmission = pointagesDuJour.size() % 2 != 0;
            if (aUneOmission) {
                detecterOmissionAvecIA(employe, jour, pointagesDuJour);
            } else if (horaireDuJourOpt.isPresent()) {
                Horaire horaireDuJour = horaireDuJourOpt.get();
                
                detecterRetard(employe, jour, pointagesDuJour, horaireDuJour);
                detecterSortieAnticipee(employe, jour, pointagesDuJour, horaireDuJour);
                detecterHeureSupplementaireAvecIA(employe, jour, pointagesDuJour, horaireDuJour);
            }
        }
        logger.info("Détection des anomalies terminée pour le jour : {}", jour);
    }
    
    private void detecterAbsenceInjustifiee(Employe employe, LocalDate jour) {
        Planning planning = employe.getPlanning();
        boolean devaitTravailler = planning != null && planning.getHorairePourJour(jour.getDayOfWeek()).isPresent();
        boolean estFerie = jourFerieRepository.existsByDate(jour);
        if (devaitTravailler && !estFerie) {
            logger.info("Absence injustifiée détectée pour le badge {} le {}.", employe.getBadge(), jour);
            String message = "Aucun pointage détecté pour une journée de travail planifiée.";
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.ABSENCE_INJUSTIFIEE, message, "Absence à justifier ou à déclarer.", null);
        }
    }
    
    // --- MÉTHODE DE DÉTECTION DE RETARD MISE À JOUR ---
    private void detecterRetard(Employe employe, LocalDate jour, List<Pointage> pointagesDuJour, Horaire horaireDuJour) {
        LocalTime heureDebutTheorique = horaireDuJour.getHeureDebutTheorique();
        if (heureDebutTheorique == null) return;

        Pointage premierPointage = pointagesDuJour.get(0);
        LocalTime heurePremierPointage = premierPointage.getDateMouvement().toLocalTime();
        int toleranceEnMinutes = 0;
        
        if (heurePremierPointage.isAfter(heureDebutTheorique.plusMinutes(toleranceEnMinutes))) {
            long dureeRetardMinutes = ChronoUnit.MINUTES.between(heureDebutTheorique, heurePremierPointage);
            logger.info("Retard détecté pour le badge {}. Durée: {} minutes. Appel de l'IA...", employe.getBadge(), dureeRetardMinutes);
            String message = String.format("Arrivée à %s au lieu de %s. Retard de %d minutes.", heurePremierPointage.format(DateTimeFormatter.ofPattern("HH:mm")), heureDebutTheorique.format(DateTimeFormatter.ofPattern("HH:mm")), dureeRetardMinutes);
            String suggestionTexte = "Validation manuelle requise.";
            try {
                // On prépare le corps de la requête avec toutes les features nécessaires au modèle
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("badge", employe.getBadge());
                requestBody.put("jour_semaine", jour.getDayOfWeek().getValue() - 1);
                requestBody.put("duree_retard_minutes", dureeRetardMinutes);
                
                // On ajoute les features utilisées par le modèle entraîné
                if (employe.getPlanning() != null) {
                    requestBody.put("PLANNING_FK", employe.getPlanning().getId());
                } else {
                    requestBody.put("PLANNING_FK", 0);
                }
                requestBody.put("nombre_pointages", pointagesDuJour.size());
                
                IaSuggestionResponse suggestion = restClient.post().uri("/predict/retard").contentType(MediaType.APPLICATION_JSON).body(requestBody).retrieve().body(IaSuggestionResponse.class);
                if (suggestion != null) {
                    suggestionTexte = String.format("Suggestion IA : %s (Confiance : %s)", suggestion.getDecision(), suggestion.getConfiance());
                }
            } catch (Exception e) {
                logger.error("Erreur de communication avec le service de prédiction de retard de l'IA. Message: {}", e.getMessage());
                suggestionTexte = "Suggestion non disponible (IA injoignable)";
            }
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.RETARD, message, suggestionTexte, null);
        }
    }

    private void detecterSortieAnticipee(Employe employe, LocalDate jour, List<Pointage> pointagesDuJour, Horaire horaireDuJour) {
        LocalTime heureDebutTheorique = horaireDuJour.getHeureDebutTheorique();
        LocalTime heureFinTheorique = horaireDuJour.getHeureFinTheorique();

        if (heureFinTheorique == null) return;
        if (heureDebutTheorique != null && heureFinTheorique.isBefore(heureDebutTheorique)) {
            logger.warn("Horaire incohérent (fin avant début) pour le badge {} le {}. Détection de sortie anticipée annulée.", employe.getBadge(), jour);
            return;
        }
        
        Pointage dernierPointage = pointagesDuJour.get(pointagesDuJour.size() - 1);
        LocalTime heureDernierPointage = dernierPointage.getDateMouvement().toLocalTime();
        int toleranceEnMinutes = 0;
        
        if (heureDernierPointage.isBefore(heureFinTheorique.minusMinutes(toleranceEnMinutes))) {
            long dureeAnticipationMinutes = ChronoUnit.MINUTES.between(heureDernierPointage, heureFinTheorique);
            logger.info("Sortie anticipée détectée pour le badge {}. Durée: {} minutes.", employe.getBadge(), dureeAnticipationMinutes);
            String message = String.format("Sortie à %s au lieu de %s. Départ anticipé de %d minutes.", heureDernierPointage.format(DateTimeFormatter.ofPattern("HH:mm")), heureFinTheorique.format(DateTimeFormatter.ofPattern("HH:mm")), dureeAnticipationMinutes);
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.SORTIE_ANTICIPEE, message, "Justification requise.", null);
        }
    }
    
    // ... Le reste de tes méthodes reste identique ...

    private void detecterOmissionAvecIA(Employe employe, LocalDate jour, List<Pointage> pointages) {
        String message = "Nombre de pointages impair détecté (" + pointages.size() + " pointages).";
        String suggestionTexte = "Validation manuelle requise.";
        LocalTime suggestionHeure = null;
        try {
            logger.info("Tentative d'appel à l'IA pour l'omission du badge {} le {}", employe.getBadge(), jour);
            Map<String, Object> requestBody = new HashMap<>();
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            requestBody.put("jour_de_semaine", jour.getDayOfWeek().getValue());
            requestBody.put("jour_du_mois", jour.getDayOfMonth());
            requestBody.put("mois", jour.getMonthValue());
            requestBody.put("semaine_de_annee", jour.get(weekFields.weekOfWeekBasedYear()));
            requestBody.put("badge", employe.getBadge());
            PredictionResponse response = restClient.post().uri("/predict/entree").contentType(MediaType.APPLICATION_JSON).body(requestBody).retrieve().body(PredictionResponse.class);
            if (response != null && response.getSuggestionHeure() != null && !response.getSuggestionHeure().isEmpty()) {
                suggestionTexte = "Suggestion IA: " + response.getSuggestionHeure();
                suggestionHeure = LocalTime.parse(response.getSuggestionHeure());
            }
        } catch (Exception e) {
            suggestionTexte = "Suggestion non disponible (IA injoignable)";
        }
        creerEtSauverAnomalie(employe, jour, TypeAnomalie.OMISSION_POINTAGE, message, suggestionTexte, suggestionHeure);
    }
    
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
        LocalTime heureDebutTheorique = horaire.getHeureDebutTheorique();
        LocalTime heureFinTheorique = horaire.getHeureFinTheorique();
        if (heureDebutTheorique == null || heureFinTheorique == null) return;
        Pointage premierPointage = pointages.get(0);
        Pointage dernierPointage = pointages.get(pointages.size() - 1);
        long dureeTravailleeSecondes = Duration.between(premierPointage.getDateMouvement(), dernierPointage.getDateMouvement()).toSeconds();
        long dureeTheoriqueSecondes = Duration.between(heureDebutTheorique, heureFinTheorique).getSeconds();
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
                    }
                } catch (Exception e) {
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