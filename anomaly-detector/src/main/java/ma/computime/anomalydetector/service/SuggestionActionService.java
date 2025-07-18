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

        switch (anomalie.getTypeAnomalie()) {
            case OMISSION_POINTAGE:
                return appliquerSuggestionOmission(anomalie);

            case HEURE_SUP_NON_AUTORISEE:
                return appliquerSuggestionSimpleValidation(anomalie, "Heure supplémentaire");

            case RETARD:
                return appliquerSuggestionSimpleValidation(anomalie, "Retard");

            case SORTIE_ANTICIPEE:
                return appliquerSuggestionSimpleValidation(anomalie, "Sortie anticipée");
                
            case TRAVAIL_JOUR_FERIE:
            case TRAVAIL_JOUR_REPOS:
                return appliquerSuggestionSimpleValidation(anomalie, "Travail jour non-ouvrable");

            case ABSENCE_INJUSTIFIEE:
                return appliquerSuggestionSimpleValidation(anomalie, "Absence injustifiée");

            default:
                logger.warn("Aucune action d'acceptation de suggestion n'est définie pour le type d'anomalie : {}", anomalie.getTypeAnomalie());
                return Optional.empty();
        }
    }
    
    

    private Optional<Anomalie> appliquerSuggestionOmission(Anomalie anomalie) {
        if (anomalie.getValeurSuggestion() == null) {
            logger.warn("Suggestion pour OMISSION (ID {}) non applicable car pas de valeur d'heure.", anomalie.getId());
            return Optional.empty();
        }
        
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
    
    private Optional<Anomalie> appliquerSuggestionSimpleValidation(Anomalie anomalie, String typeDescription) {
        logger.info("Application d'une validation simple pour {} (ID {}).", typeDescription, anomalie.getId());

        anomalie.setStatut(StatutAnomalie.VALIDEE);
        anomalie.setCommentaireValidation(typeDescription + " validé(e) suite à la suggestion de l'IA. Décision IA : " + anomalie.getDecisionIa());
        anomalie.setDateResolution(LocalDateTime.now());
        anomalieRepository.save(anomalie);

        logger.info("Anomalie de type {} ID {} validée.", typeDescription, anomalie.getId());
        return Optional.of(anomalie);
    }
}