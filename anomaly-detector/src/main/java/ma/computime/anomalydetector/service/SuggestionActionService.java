// Emplacement : src/main/java/ma/computime/anomalydetector/service/SuggestionActionService.java
package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.Pointage;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.entity.TypeAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SuggestionActionService {

    private static final Logger logger = LoggerFactory.getLogger(SuggestionActionService.class);

    @Autowired
    private AnomalieRepository anomalieRepository;

    @Autowired
    private PointageRepository pointageRepository;

    @Transactional
    public Optional<Anomalie> accepterSuggestion(Long anomalieId) {
        logger.info("Tentative d'acceptation de la suggestion pour l'anomalie ID : {}", anomalieId);

        Optional<Anomalie> anomalieOpt = anomalieRepository.findById(anomalieId);

        if (anomalieOpt.isEmpty() || anomalieOpt.get().getStatut() != StatutAnomalie.EN_ATTENTE) {
            logger.warn("Échec : Anomalie ID {} non trouvée ou déjà traitée.", anomalieId);
            return Optional.empty();
        }

        Anomalie anomalie = anomalieOpt.get();
        TypeAnomalie type = anomalie.getTypeAnomalie();

        // =================================================================
        // === NOUVELLE LOGIQUE : On traite chaque type d'anomalie différemment ===
        // =================================================================
        
        // --- CAS 1 : C'est une OMISSION DE POINTAGE ---
        if (type == TypeAnomalie.OMISSION_POINTAGE) {
            if (anomalie.getValeurSuggestion() != null) {
                return appliquerSuggestionOmission(anomalie);
            } else {
                logger.warn("Suggestion pour OMISSION (ID {}) non applicable car pas de valeur d'heure.", anomalieId);
                return Optional.empty();
            }
        }

        // --- CAS 2 : C'est une HEURE SUPPLÉMENTAIRE ---
        if (type == TypeAnomalie.HEURE_SUP_NON_AUTORISEE) {
            // Pour une HS, "accepter la suggestion" signifie simplement valider l'anomalie.
            return appliquerSuggestionHeureSup(anomalie);
        }
        
        // (Plus tard, on pourra ajouter d'autres "if" pour d'autres types d'anomalies)

        logger.warn("Aucune action d'acceptation de suggestion n'est définie pour le type d'anomalie : {}", type);
        return Optional.empty();
    }

    private Optional<Anomalie> appliquerSuggestionOmission(Anomalie anomalie) {
        logger.info("Application de la suggestion pour OMISSION (ID {}): création d'un pointage.", anomalie.getId());

        Pointage nouveauPointage = new Pointage();
        nouveauPointage.setEmploye(anomalie.getEmploye());
        nouveauPointage.setBadgeEmploye(anomalie.getEmploye().getBadge());
        LocalDateTime dateHeurePointage = LocalDateTime.of(anomalie.getJourAnomalie(), anomalie.getValeurSuggestion());
        nouveauPointage.setDateMouvement(dateHeurePointage);
        nouveauPointage.setCodePointeuse("MANUEL_IA");
        pointageRepository.save(nouveauPointage);

        anomalie.setStatut(StatutAnomalie.RESOLUE);
        anomalie.setCommentaireValidation("Suggestion de l'IA acceptée. Pointage manquant ajouté.");
        anomalie.setDateResolution(LocalDateTime.now());
        anomalieRepository.save(anomalie);
        
        logger.info("Pointage créé et anomalie ID {} résolue.", anomalie.getId());
        return Optional.of(anomalie);
    }
    
    private Optional<Anomalie> appliquerSuggestionHeureSup(Anomalie anomalie) {
        logger.info("Application de la suggestion pour HEURE_SUP (ID {}): validation de l'anomalie.", anomalie.getId());

        anomalie.setStatut(StatutAnomalie.VALIDEE); // Pour une HS, accepter = valider
        anomalie.setCommentaireValidation("Heure supplémentaire validée suite à la suggestion de l'IA.");
        anomalie.setDateResolution(LocalDateTime.now());
        anomalieRepository.save(anomalie);

        logger.info("Anomalie d'heure supplémentaire ID {} validée.", anomalie.getId());
        return Optional.of(anomalie);
    }
}