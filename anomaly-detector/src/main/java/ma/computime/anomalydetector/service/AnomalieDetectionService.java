// Emplacement : src/main/java/ma/computime/anomalydetector/service/AnomalieDetectionService.java

package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.dto.IaSuggestionResponse;
import ma.computime.anomalydetector.dto.PredictionResponse;
import ma.computime.anomalydetector.entity.*;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.JourFerieRepository;
import ma.computime.anomalydetector.repository.PlanningExceptionRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
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
    @Autowired private PlanningExceptionRepository planningExceptionRepository;

    @Transactional
    public void lancerDetectionPourTous(LocalDate jour) {
        logger.info("Lancement de la détection des anomalies pour le jour : {}", jour);
        
        List<Employe> employes = employeRepository.findAll();

        for (Employe employe : employes) {
            List<Pointage> pointagesDuJour = pointageRepository.findForBadgeOnDay(
                    employe.getBadge(),
                    jour
            ).stream()
             .sorted(Comparator.comparing(Pointage::getDateMouvement))
             .collect(Collectors.toList());

            // 1. Déterminer le planning actif (normal ou exceptionnel)
            Planning planningActif = employe.getPlanning(); 
            Optional<PlanningException> exceptionOpt = planningExceptionRepository.findActiveExceptionForEmploye(employe, jour);

            if (exceptionOpt.isPresent()) {
                PlanningException exception = exceptionOpt.get();
                if (exception.getPlanning() != null) {
                    planningActif = exception.getPlanning();
                    logger.info("-> Exception (ID: {}) trouvée pour l'employé {}. Utilisation du planning exceptionnel ID: {}.",
                            exception.getId(), employe.getBadge(), planningActif.getId());
                } else {
                    logger.warn("-> Exception (ID: {}) trouvée pour l'employé {} mais elle n'a pas de planning associé. Utilisation du planning normal.",
                            exception.getId(), employe.getBadge());
                }
            }

            // 2. Vérifier si c'est un jour non travaillé (Férié ou Repos selon le planning actif)
            boolean estUnJourOff = detecterTravailJourNonTravaille(employe, jour, pointagesDuJour, planningActif);
            if (estUnJourOff) {
                detecterHeureSupplementaireAvecIA(employe, jour, pointagesDuJour, null);
                continue;
            }

            // 3. Gérer l'absence si aucun pointage une journée où il fallait travailler
            if (pointagesDuJour.isEmpty()) {
                detecterAbsenceInjustifiee(employe, jour, planningActif);
                continue;
            }
            
            // 4. Si on arrive ici, l'employé a pointé et devait travailler. On a besoin d'un planning pour continuer.
            if (planningActif == null) {
                logger.warn("L'employé avec badge {} n'a pas de planning affecté (ni d'exception valide). Anomalies basées sur les horaires non détectées.", employe.getBadge());
                continue;
            }

            // 5. Récupérer l'horaire du jour depuis le planning actif
            Optional<Horaire> horaireDuJourOpt = planningActif.getHorairePourJour(jour.getDayOfWeek());

            if (horaireDuJourOpt.isPresent()) {
                Horaire horaireDuJour = horaireDuJourOpt.get();
                
                // 6. Lancer les détections basées sur les horaires
                if (pointagesDuJour.size() % 2 != 0) {
                    detecterOmissionAvecIA(employe, jour);
                } else {
                    detecterRetard(employe, jour, pointagesDuJour, horaireDuJour);
                    detecterSortieAnticipeeAvecIA(employe, jour, pointagesDuJour, horaireDuJour);
                    detecterHeureSupplementaireAvecIA(employe, jour, pointagesDuJour, horaireDuJour);
                    detecterPauseTropLongue(employe, jour, pointagesDuJour, horaireDuJour);
                }
            } else {
                 logger.warn("Aucun horaire défini pour {} dans le planning actif ID {} pour l'employé {}.",
                        jour.getDayOfWeek(), planningActif.getId(), employe.getBadge());
            }
        }
        logger.info("Détection des anomalies terminée pour le jour : {}", jour);
    }
    
    // =================================================================================
    // MÉTHODE DE CRÉATION D'ANOMALIE (INCHANGÉE)
    // =================================================================================
    private void creerEtSauverAnomalie(Employe employe, LocalDate jour, TypeAnomalie type, String message, String suggestion, LocalTime valeurSuggestion, Long dureeEnMinutes) {
        if (anomalieRepository.existsByEmployeAndJourAnomalieAndTypeAnomalie(employe, jour, type)) {
            logger.warn("DUPLICATA: Anomalie de type {} existe déjà pour l'employé {} le {}. Ignorée.", type, employe.getBadge(), jour);
            return;
        }
        Anomalie anomalie = new Anomalie();
        anomalie.setEmploye(employe);
        if (employe.getNoeud() != null) {
            anomalie.setNoeudConcerne(employe.getNoeud());
        } else {
             logger.warn("L'employé {} n'est assigné à aucun noeud. L'anomalie ne sera pas assignée.", employe.getBadge());
        }
        anomalie.setJourAnomalie(jour);
        anomalie.setTypeAnomalie(type);
        anomalie.setMessage(message);
        anomalie.setSuggestion(suggestion);
        anomalie.setValeurSuggestion(valeurSuggestion);
        anomalie.setStatut(StatutAnomalie.EN_ATTENTE);
        anomalie.setDureeEnMinutes(dureeEnMinutes);
        anomalieRepository.save(anomalie);
        logger.info("Anomalie de type {} sauvegardée pour l'employé {}.", type, employe.getBadge());
    }

    // =================================================================================
    // MÉTHODES DE DÉTECTION SPÉCIFIQUES (CORRIGÉES ET COHÉRENTES)
    // =================================================================================

    private boolean detecterTravailJourNonTravaille(Employe employe, LocalDate jour, List<Pointage> pointages, Planning planning) {
        if (pointages.isEmpty()) return false;
    
        String typeJourNonTravaille = null;
        TypeAnomalie typeAnomalie = null;
    
        if (jourFerieRepository.existsByDate(jour)) {
            typeJourNonTravaille = "FERIE";
            typeAnomalie = TypeAnomalie.TRAVAIL_JOUR_FERIE;
        } else {
            // L'erreur "Duplicate local variable" était ici. On utilise directement le 'planning' reçu en argument.
            if (planning != null && planning.getHorairePourJour(jour.getDayOfWeek()).isEmpty()) {
                typeJourNonTravaille = "REPOS";
                typeAnomalie = TypeAnomalie.TRAVAIL_JOUR_REPOS;
            }
        }
        
        if (typeJourNonTravaille != null && typeAnomalie != null) {
            if (pointages.size() < 2) {
                String message = "Pointage unique détecté un " + typeJourNonTravaille + ".";
                creerEtSauverAnomalie(employe, jour, typeAnomalie, message, "Pointage à vérifier ou supprimer.", null, 0L);
                return true;
            }
            
            long dureeTravailleeMinutes = Duration.between(pointages.get(0).getDateMouvement(), pointages.get(pointages.size() - 1).getDateMouvement()).toMinutes();
            double dureeTravailleeHeures = dureeTravailleeMinutes / 60.0;
            double soldeReposActuelHeures = 1.5; 
    
            Map<String, Object> context = new HashMap<>();
            context.put("type_jour", typeJourNonTravaille);
            context.put("duree_travaillee_heures", dureeTravailleeHeures);
            context.put("solde_repos_actuel_heures", soldeReposActuelHeures);
    
            String suggestionTexte = callIaService("/predict/compensation", context, "prédiction de compensation");
            String message = String.format("Pointages détectés un %s. Durée travaillée: %.2f heures.", typeJourNonTravaille, dureeTravailleeHeures);
            creerEtSauverAnomalie(employe, jour, typeAnomalie, message, suggestionTexte, null, dureeTravailleeMinutes);
            return true; 
        }
        return false;
    }

    // Dans AnomalieDetectionService.java

// Dans AnomalieDetectionService.java

private void detecterAbsenceInjustifiee(Employe employe, LocalDate jour, Planning planning) {
        // Vérifie si l'employé devait travailler ce jour-là
        boolean devaitTravailler = planning != null && planning.getHorairePourJour(jour.getDayOfWeek()).isPresent();
        
        if (devaitTravailler) {
            // --- LOGIQUE IA AVEC VRAIES DONNÉES ---
            
            // 1. Collecter le contexte RÉEL depuis la base de données
            Map<String, Object> context = new HashMap<>();

            // Feature 1: Durée de l'absence (pour l'instant, on détecte au jour le jour)
            context.put("duree_absence_jours", 1); // AJOUTÉ : C'est une feature attendue par le modèle

            // Feature 2: Solde de congés actuel de l'employé
            double soldeConges = employe.getSoldeConges() != null ? employe.getSoldeConges() : 0.0;
            context.put("solde_conges_jours", soldeConges); // MODIFIÉ : Utilise la vraie valeur

            // Feature 3: Nombre d'absences injustifiées déjà validées cette année
            long nbAbsencesAnnee = anomalieRepository.countByEmployeAndTypeAnomalieAndStatutInYear(
                employe, 
                TypeAnomalie.ABSENCE_INJUSTIFIEE, 
                StatutAnomalie.VALIDEE,
                jour.getYear()
            );
            context.put("nb_absences_injustifiees_annee", nbAbsencesAnnee); // MODIFIÉ : Utilise un vrai appel DB

            // Feature 4: Vérifier si l'absence est collée à un week-end ou jour férié
            boolean estAdjacentWeekend = jour.getDayOfWeek() == DayOfWeek.MONDAY || jour.getDayOfWeek() == DayOfWeek.FRIDAY;
            boolean estAdjacentFerie = jourFerieRepository.existsByDate(jour.minusDays(1)) || jourFerieRepository.existsByDate(jour.plusDays(1));
            context.put("est_adjacent_weekend_ferie", (estAdjacentWeekend || estAdjacentFerie) ? 1 : 0); // MODIFIÉ : Logique plus complète

            // Feature 5: Charge de l'équipe (on garde la simulation pour celle-ci)
            context.put("charge_equipe", 0.7); // MODIFIÉ : Nom de feature aligné sur le modèle

            // 2. Appeler l'IA avec le contexte réel
            String suggestionTexte = callIaService("/predict/absence-injustifiee", context, "prédiction d'absence");
            
            // 3. Créer l'anomalie avec la suggestion de l'IA
            String message = "Aucun pointage détecté pour une journée de travail planifiée.";
            
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.ABSENCE_INJUSTIFIEE, message, suggestionTexte, null, null);
        }
    }

    private void detecterSortieAnticipeeAvecIA(Employe employe, LocalDate jour, List<Pointage> pointagesDuJour, Horaire horaireDuJour) {
        LocalTime heureFinTheorique = horaireDuJour.getHeureFinTheorique();
        if (heureFinTheorique == null || pointagesDuJour.size() < 2) return;
        LocalTime heureDernierPointage = pointagesDuJour.get(pointagesDuJour.size() - 1).getDateMouvement().toLocalTime();
        if (heureDernierPointage.isBefore(heureFinTheorique)) {
            long dureeAnticipationMinutes = ChronoUnit.MINUTES.between(heureDernierPointage, heureFinTheorique);
            if (dureeAnticipationMinutes > 5) {
                Map<String, Object> context = new HashMap<>();
                context.put("duree_anticipation_minutes", dureeAnticipationMinutes);
                context.put("heure_sortie_reelle", heureDernierPointage.getHour());
                context.put("est_fin_semaine", (jour.getDayOfWeek() == DayOfWeek.FRIDAY || jour.getDayOfWeek() == DayOfWeek.SATURDAY) ? 1 : 0);
                long nbHeuresSupValidees = anomalieRepository.countByEmployeAndTypeAnomalieAndStatut(employe, TypeAnomalie.HEURE_SUP_NON_AUTORISEE, StatutAnomalie.VALIDEE);
                context.put("nb_hs_recentes_heures", nbHeuresSupValidees);
                context.put("charge_travail_jour", 0.75);
                String suggestionTexte = callIaService("/predict/sortie-anticipee-context", context, "prédiction de sortie anticipée");
                String message = String.format("Sortie à %s au lieu de %s. Départ anticipé de %d minutes.", heureDernierPointage.format(DateTimeFormatter.ofPattern("HH:mm")), heureFinTheorique.format(DateTimeFormatter.ofPattern("HH:mm")), dureeAnticipationMinutes);
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.SORTIE_ANTICIPEE, message, suggestionTexte, null, dureeAnticipationMinutes);
            }
        }
    }

    private void detecterOmissionAvecIA(Employe employe, LocalDate jour) {
        String message = "Nombre de pointages impair détecté.";
        LocalTime suggestionHeure = null;
        String suggestionTexte = "Validation manuelle requise.";
        try {
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
        creerEtSauverAnomalie(employe, jour, TypeAnomalie.OMISSION_POINTAGE, message, suggestionTexte, suggestionHeure, null);
    }
    
    private void detecterRetard(Employe employe, LocalDate jour, List<Pointage> pointagesDuJour, Horaire horaireDuJour) {
        LocalTime heureDebutTheorique = horaireDuJour.getHeureDebutTheorique();
        if (heureDebutTheorique == null || pointagesDuJour.isEmpty()) return;
        LocalTime heurePremierPointage = pointagesDuJour.get(0).getDateMouvement().toLocalTime();
        int toleranceEnMinutes = Optional.ofNullable(horaireDuJour.getToleranceEntree()).orElse(0);
        if (heurePremierPointage.isAfter(heureDebutTheorique.plusMinutes(toleranceEnMinutes))) {
            long dureeRetardMinutes = ChronoUnit.MINUTES.between(heureDebutTheorique, heurePremierPointage);
            if (dureeRetardMinutes <= 0) return;
            long nbRetardsValides = anomalieRepository.countByEmployeAndTypeAnomalieAndStatut(employe, TypeAnomalie.RETARD, StatutAnomalie.VALIDEE);
            Map<String, Object> requestBody = Map.of("duree_retard_minutes", dureeRetardMinutes, "nb_retards_mois_precedent", nbRetardsValides, "est_debut_semaine", (jour.getDayOfWeek() == DayOfWeek.MONDAY) ? 1 : 0, "charge_travail_equipe_jour", 0.5);
            String suggestionTexte = callIaService("/predict/retard-context", requestBody, "prédiction de retard");
            String message = String.format("Arrivée à %s au lieu de %s. Retard de %d minutes.", heurePremierPointage.format(DateTimeFormatter.ofPattern("HH:mm")), heureDebutTheorique.format(DateTimeFormatter.ofPattern("HH:mm")), dureeRetardMinutes);
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.RETARD, message, suggestionTexte, null, dureeRetardMinutes);
        }
    }

    private void detecterHeureSupplementaireAvecIA(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire) {
        if (pointages.size() < 2) return;
        long dureeTravailleeMinutes = Duration.between(pointages.get(0).getDateMouvement(), pointages.get(pointages.size() - 1).getDateMouvement()).toMinutes();
        long dureeTheoriqueMinutes = 0;
        if (horaire != null && horaire.getHeureDebutTheorique() != null && horaire.getHeureFinTheorique() != null) {
            dureeTheoriqueMinutes = Duration.between(horaire.getHeureDebutTheorique(), horaire.getHeureFinTheorique()).toMinutes() - Optional.ofNullable(horaire.getDureeTotalePauseMinutes()).orElse(0);
        }
        if (dureeTravailleeMinutes > dureeTheoriqueMinutes) {
            long hsMinutes = dureeTravailleeMinutes - dureeTheoriqueMinutes;
            if (hsMinutes < 120) {
                logger.info("Heure supplémentaire de {} minutes pour le badge {} ignorée (inférieure au seuil de 120 min).", hsMinutes, employe.getBadge());
                return;
            }
            long hsPasseesValidees = anomalieRepository.countByEmployeAndTypeAnomalieAndStatut(employe, TypeAnomalie.HEURE_SUP_NON_AUTORISEE, StatutAnomalie.VALIDEE);
            Map<String, Object> requestBody = Map.of("badge", employe.getBadge(), "hs_minutes", hsMinutes, "taux_absence_service", 0.1, "nb_hs_validees_historique", hsPasseesValidees);
            String suggestionTexte = callIaService("/predict/overtime-context", requestBody, "prédiction d'heures sup");
            String message = String.format("Heure supplémentaire de %d minutes détectée.", hsMinutes);
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.HEURE_SUP_NON_AUTORISEE, message, suggestionTexte, null, hsMinutes);
        }
    }

    private void detecterPauseTropLongue(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire) {
        if (pointages.size() < 4) return;
        Integer dureePauseAutorisee = Optional.ofNullable(horaire.getDureeTotalePauseMinutes()).orElse(0);
        if (dureePauseAutorisee <= 0) return;
        for (int i = 1; i < pointages.size() - 1; i += 2) {
            long dureePauseEffectiveMinutes = Duration.between(pointages.get(i).getDateMouvement(), pointages.get(i + 1).getDateMouvement()).toMinutes();
            if (dureePauseEffectiveMinutes > dureePauseAutorisee) {
                long depassement = dureePauseEffectiveMinutes - dureePauseAutorisee;
                String message = String.format("Pause de %d minutes détectée (dépassement de %d min). Autorisée: %d min.", dureePauseEffectiveMinutes, depassement, dureePauseAutorisee);
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.PAUSE_TROP_LONGUE, message, "Validation requise.", null, depassement);
                break; 
            }
        }
    }
    
    private String callIaService(String uri, Map<String, Object> requestBody, String logContext) {
        try {
            IaSuggestionResponse suggestion = restClient.post().uri(uri).contentType(MediaType.APPLICATION_JSON).body(requestBody).retrieve().body(IaSuggestionResponse.class);
            if (suggestion != null && suggestion.getDecision() != null) {
                return String.format("Suggestion IA : %s (%s). Justification : %s", suggestion.getDecision(), suggestion.getConfiance(), String.join(" | ", suggestion.getJustification()));
            }
        } catch (Exception e) {
            logger.error("Erreur de communication avec le service de {}. Message: {}", logContext, e.getMessage());
        }
        return "Suggestion non disponible (IA injoignable)";
    }
}