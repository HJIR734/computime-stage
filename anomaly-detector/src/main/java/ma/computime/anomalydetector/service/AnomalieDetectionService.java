// Emplacement : src/main/java/ma/computime/anomalydetector/service/AnomalieDetectionService.java

package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.dto.IaSuggestionResponse;
import ma.computime.anomalydetector.dto.PredictionAbsenceResponse;
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
    @Autowired private PlanningExceptionRepository planningExceptionRepository;
    

    @Transactional
    // Dans AnomalieDetectionService.java


// Dans AnomalieDetectionService.java

public void lancerDetectionPourTous(LocalDate jour) {
    logger.info("Lancement de la détection des anomalies pour le jour : {}", jour);
    
    List<Employe> employes = employeRepository.findAll();

    for (Employe employe : employes) {
        
        // =========================================================================
        // ÉTAPE 0 : TROUVER LE MANAGER DE L'EMPLOYÉ (AJOUTÉ)
        // =========================================================================
        Optional<Employe> managerOpt = trouverManagerPourEmploye(employe);

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
        // NOTE: On passe 'managerOpt' aux méthodes de détection pour qu'elles puissent l'assigner.
        boolean estUnJourOff = detecterTravailJourNonTravaille(employe, jour, pointagesDuJour, planningActif, managerOpt);
        if (estUnJourOff) {
            detecterHeureSupplementaireAvecIA(employe, jour, pointagesDuJour, null, managerOpt);
            continue;
        }

        // 3. Gérer l'absence si aucun pointage une journée où il fallait travailler
        if (pointagesDuJour.isEmpty()) {
            detecterAbsenceInjustifiee(employe, jour, planningActif, managerOpt);
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
                detecterOmissionAvecIA(employe, jour, managerOpt);
            } else {
                detecterRetard(employe, jour, pointagesDuJour, horaireDuJour, managerOpt);
                detecterSortieAnticipeeAvecIA(employe, jour, pointagesDuJour, horaireDuJour, managerOpt);
                detecterHeureSupplementaireAvecIA(employe, jour, pointagesDuJour, horaireDuJour, managerOpt);
                detecterPauseTropLongue(employe, jour, pointagesDuJour, horaireDuJour, managerOpt);
            }
        } else {
             logger.warn("Aucun horaire défini pour {} dans le planning actif ID {} pour l'employé {}.",
                    jour.getDayOfWeek(), planningActif.getId(), employe.getBadge());
        }
    }
    logger.info("Détection des anomalies terminée pour le jour : {}", jour);
}
    
   // =================================================================================
// MÉTHODE CENTRALE DE CRÉATION D'ANOMALIE (VERSION FINALE À COPIER)
// =================================================================================
// Dans AnomalieDetectionService.java

// =================================================================================
// MÉTHODE CENTRALE DE CRÉATION D'ANOMALIE (VERSION FINALE CORRIGÉE)
// =================================================================================
// Dans AnomalieDetectionService.java

// ... (tous tes imports en haut)

// =================================================================================
// MÉTHODE POUR TROUVER LE MANAGER (VERSION FINALE ET SIMPLE)
// =================================================================================
// =================================================================================
// MÉTHODE POUR TROUVER LE MANAGER (VERSION FINALE CORRIGÉE - À REMPLACER)
// =================================================================================
private Optional<Employe> trouverManagerPourEmploye(Employe employe) {
    // Étape 1: Vérifier si l'employé a un noeud.
    if (employe.getNoeud() == null) {
        logger.warn("L'employé {} n'est rattaché à aucun noeud. Impossible de trouver un manager.", employe.getBadge());
        return Optional.empty();
    }

    // Étape 2: Récupérer le noeud de l'employé et trouver son noeud parent.
    Noeud noeudEmploye = employe.getNoeud();
    Noeud noeudParent = noeudEmploye.getNoeudParent(); // En supposant que tu as bien un champ "noeudParent" dans ton entité Noeud

    if (noeudParent == null) {
        logger.warn("Le noeud {} de l'employé {} n'a pas de noeud parent. L'employé est peut-être déjà un manager.", noeudEmploye.getId(), employe.getBadge());
        return Optional.empty();
    }

    // Étape 3: Trouver tous les employés qui appartiennent à ce noeud parent.
    // Ce sont les managers potentiels.
    List<Employe> managersPotentiels = employeRepository.findByNoeud(noeudParent);

    // Étape 4: On retourne le premier manager trouvé dans la liste.
    // Le filtre pour s'assurer qu'il n'est pas l'employé lui-même est une bonne sécurité.
    return managersPotentiels.stream()
            .filter(manager -> !manager.getId().equals(employe.getId()))
            .findFirst();
}


// =================================================================================
// MÉTHODE POUR CRÉER L'ANOMALIE (VERSION FINALE ET SIMPLE)
// =================================================================================
// =================================================================================
// MÉTHODE POUR CRÉER L'ANOMALIE (VERSION FINALE ET SIMPLE - À REMPLACER)
// =================================================================================
private void creerEtSauverAnomalie(Employe employe, LocalDate jour, TypeAnomalie type, String message, String suggestion, LocalTime valeurSuggestion, Long dureeEnMinutes, Optional<Employe> managerOpt) {
    if (anomalieRepository.existsByEmployeAndJourAnomalieAndTypeAnomalie(employe, jour, type)) {
        logger.warn("DUPLICATA: Anomalie de type {} existe déjà pour l'employé {} le {}. Ignorée.", type, employe.getBadge(), jour);
        return;
    }

    Anomalie anomalie = new Anomalie();
    anomalie.setEmploye(employe);
    anomalie.setNoeudConcerne(employe.getNoeud()); // C'est une bonne pratique de garder le noeud de l'employé
    anomalie.setJourAnomalie(jour);
    anomalie.setTypeAnomalie(type);
    anomalie.setMessage(message);
    anomalie.setSuggestion(suggestion);
    anomalie.setValeurSuggestion(valeurSuggestion);
    anomalie.setStatut(StatutAnomalie.EN_ATTENTE);
    anomalie.setDureeEnMinutes(dureeEnMinutes);
    anomalie.setDateTraitement(null);
    anomalie.setCommentaireManager(null);

    // =========================================================
    // CORRECTION LOGIQUE : Une seule façon d'assigner le manager
    // =========================================================
    // On assigne le manager à l'anomalie SI un manager a été trouvé.
    managerOpt.ifPresent(anomalie::setManagerResponsable); 

    anomalieRepository.save(anomalie);
    
    // Log amélioré pour confirmer l'assignation
    managerOpt.ifPresentOrElse(
        manager -> logger.info("Anomalie de type {} sauvegardée pour l'employé {} et assignée au manager {}.", type, employe.getBadge(), manager.getBadge()),
        () -> logger.info("Anomalie de type {} sauvegardée pour l'employé {} (non assignée).", type, employe.getBadge())
    );
}



    // =================================================================================
    // MÉTHODES DE DÉTECTION SPÉCIFIQUES (CORRIGÉES ET COHÉRENTES)
    // =================================================================================

    private boolean detecterTravailJourNonTravaille(Employe employe, LocalDate jour, List<Pointage> pointages, Planning planning, Optional<Employe> managerOpt) {
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
                creerEtSauverAnomalie(employe, jour, typeAnomalie, message, "Pointage à vérifier ou supprimer.", null, 0L, managerOpt);
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
            creerEtSauverAnomalie(employe, jour, typeAnomalie, message, suggestionTexte, null, dureeTravailleeMinutes, managerOpt);
            return true; 
        }
        return false;
    }

    // Dans AnomalieDetectionService.java

// Dans AnomalieDetectionService.java

private void detecterAbsenceInjustifiee(Employe employe, LocalDate jour, Planning planning, Optional<Employe> managerOpt) {
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
            
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.ABSENCE_INJUSTIFIEE, message, suggestionTexte, null, null, managerOpt);
        }
    }

    private void detecterSortieAnticipeeAvecIA(Employe employe, LocalDate jour, List<Pointage> pointagesDuJour, Horaire horaireDuJour, Optional<Employe> managerOpt) {
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
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.SORTIE_ANTICIPEE, message, suggestionTexte, null, dureeAnticipationMinutes, managerOpt);
            }
        }
    }

    // =================================================================================
// MÉTHODE DE DÉTECTION D'OMISSION (À REMPLACER)
// =================================================================================
private void detecterOmissionAvecIA(Employe employe, LocalDate jour, Optional<Employe> managerOpt) {
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
    // ========================================================================
    // CORRECTION SYNTAXE : On ajoute 'managerOpt' à la fin de l'appel
    // ========================================================================
    creerEtSauverAnomalie(employe, jour, TypeAnomalie.OMISSION_POINTAGE, message, suggestionTexte, suggestionHeure, null, managerOpt);
}
    
    private void detecterRetard(Employe employe, LocalDate jour, List<Pointage> pointagesDuJour, Horaire horaireDuJour, Optional<Employe> managerOpt) {
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
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.RETARD, message, suggestionTexte, null, dureeRetardMinutes, managerOpt);
        }
    }

    // Dans AnomalieDetectionService.java

private void detecterHeureSupplementaireAvecIA(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire, Optional<Employe> managerOpt) {
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

        // ====================== DÉBUT DE LA MODIFICATION ======================

        // Étape 1 : Récupérer le mode de compensation préféré de l'employé depuis son planning.
        String modeCompensationPrefere = "PAYE"; // Valeur par défaut si rien n'est défini.
        
        // On vérifie que l'employé a un planning et que ce planning a une catégorie HS définie.
        if (employe.getPlanning() != null && employe.getPlanning().getCategorieHeureSup() != null) {
            
            // Hypothèse basée sur tes données : 1 = COMPENSE, 0 (ou autre) = PAYE.
            if (employe.getPlanning().getCategorieHeureSup() == 1) {
                modeCompensationPrefere = "COMPENSE";
            }
        }

        // Étape 2 : Créer le corps de la requête pour l'IA en y ajoutant notre nouvelle information.
        // On ne peut plus utiliser Map.of() car il est immuable. On passe à HashMap.
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("badge", employe.getBadge());
        requestBody.put("hs_minutes", hsMinutes);
        requestBody.put("taux_absence_service", 0.1); // On garde les anciennes features pour la compatibilité
        
        long hsPasseesValidees = anomalieRepository.countByEmployeAndTypeAnomalieAndStatut(employe, TypeAnomalie.HEURE_SUP_NON_AUTORISEE, StatutAnomalie.VALIDEE);
        requestBody.put("nb_hs_validees_historique", hsPasseesValidees);
        
        // On ajoute la nouvelle feature !
        requestBody.put("mode_compensation_prefere", modeCompensationPrefere);
        
        // ======================= FIN DE LA MODIFICATION =======================

        String suggestionTexte = callIaService("/predict/overtime-context", requestBody, "prédiction d'heures sup");
        String message = String.format("Heure supplémentaire de %d minutes détectée.", hsMinutes);
        
        creerEtSauverAnomalie(employe, jour, TypeAnomalie.HEURE_SUP_NON_AUTORISEE, message, suggestionTexte, null, hsMinutes, managerOpt);
    }
}

    private void detecterPauseTropLongue(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire, Optional<Employe> managerOpt) {
        if (pointages.size() < 4) return;
        Integer dureePauseAutorisee = Optional.ofNullable(horaire.getDureeTotalePauseMinutes()).orElse(0);
        if (dureePauseAutorisee <= 0) return;
        for (int i = 1; i < pointages.size() - 1; i += 2) {
            long dureePauseEffectiveMinutes = Duration.between(pointages.get(i).getDateMouvement(), pointages.get(i + 1).getDateMouvement()).toMinutes();
            if (dureePauseEffectiveMinutes > dureePauseAutorisee) {
                long depassement = dureePauseEffectiveMinutes - dureePauseAutorisee;
                String message = String.format("Pause de %d minutes détectée (dépassement de %d min). Autorisée: %d min.", dureePauseEffectiveMinutes, depassement, dureePauseAutorisee);
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.PAUSE_TROP_LONGUE, message, "Validation requise.", null, depassement, managerOpt);
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

    public double getAbsencePrediction(LocalDate jour, Employe employe) {
    
    // 1. Préparer les features, exactement comme dans le notebook Python
    Map<String, Object> requestBody = new HashMap<>();
    
    // Features temporelles
    requestBody.put("jour_semaine", jour.getDayOfWeek().getValue() - 1); // Python: 0=Lundi, Java: 1=Lundi. On ajuste.
    requestBody.put("jour_mois", jour.getDayOfMonth());
    requestBody.put("mois", jour.getMonthValue());
    requestBody.put("semaine_annee", jour.get(WeekFields.of(Locale.FRANCE).weekOfWeekBasedYear()));

    // Features de l'employé
    requestBody.put("profil_metier_id", employe.getProfilMetier() != null ? employe.getProfilMetier().getId() : 0);
    requestBody.put("noeud_id", employe.getNoeud() != null ? employe.getNoeud().getId() : 0);

    // Features de jours spéciaux
    boolean estFerie = jourFerieRepository.existsByDate(jour);
    requestBody.put("est_ferie", estFerie ? 1 : 0);
    requestBody.put("veille_ferie", jourFerieRepository.existsByDate(jour.plusDays(1)) ? 1 : 0);
    requestBody.put("lendemain_ferie", jourFerieRepository.existsByDate(jour.minusDays(1)) ? 1 : 0);

    try {
        // 2. Appeler l'API Flask
        PredictionAbsenceResponse response = restClient.post()
                .uri("/predict/absence-future") // Le nouvel endpoint
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(PredictionAbsenceResponse.class);
        
        if (response != null) {
            return response.getProbabiliteAbsence();
        }
    } catch (Exception e) {
        logger.error("Erreur lors de l'appel à l'API de prédiction d'absence: {}", e.getMessage());
    }

    // En cas d'erreur, on renvoie une probabilité très basse
    return -1.0; // On renvoie -1 pour signifier une erreur, c'est mieux que 0.0
}
}