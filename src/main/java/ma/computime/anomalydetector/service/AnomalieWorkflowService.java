// Emplacement : src/main/java/ma/computime/anomalydetector/service/AnomalieWorkflowService.java
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
import java.time.LocalTime;
import java.util.Optional;

@Service
public class AnomalieWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(AnomalieWorkflowService.class);

    @Autowired
    private AnomalieRepository anomalieRepository;

    @Autowired
    private PointageRepository pointageRepository;

    /**
     * Valide une anomalie simple (qui ne nécessite pas de création de données), 
     * en changeant son statut à VALIDEE.
     * Ex: Valider un retard ou une sortie anticipée.
     */
    @Transactional
    public Optional<Anomalie> validerAnomalie(Long anomalieId, String commentaire) {
        logger.info("Tentative de validation de l'anomalie ID : {}", anomalieId);
        
        Optional<Anomalie> anomalieOpt = anomalieRepository.findById(anomalieId);

        if (anomalieOpt.isEmpty() || anomalieOpt.get().getStatut() != StatutAnomalie.EN_ATTENTE) {
            logger.warn("Échec de la validation : Anomalie ID {} non trouvée ou déjà traitée.", anomalieId);
            return Optional.empty();
        }

        Anomalie anomalieAValider = anomalieOpt.get();
        anomalieAValider.setStatut(StatutAnomalie.VALIDEE);
        anomalieAValider.setCommentaireValidation(commentaire);
        anomalieAValider.setDateResolution(LocalDateTime.now());

        Anomalie anomalieSauvegardee = anomalieRepository.save(anomalieAValider);
        logger.info("Anomalie ID {} validée avec succès.", anomalieId);
        
        return Optional.of(anomalieSauvegardee);
    }

    /**
     * Rejette une anomalie simple, en changeant son statut à REJETEE.
     */
    @Transactional
    public Optional<Anomalie> rejeterAnomalie(Long anomalieId, String commentaire) {
        logger.info("Tentative de rejet de l'anomalie ID : {}", anomalieId);

        Optional<Anomalie> anomalieOpt = anomalieRepository.findById(anomalieId);

        if (anomalieOpt.isEmpty() || anomalieOpt.get().getStatut() != StatutAnomalie.EN_ATTENTE) {
            logger.warn("Échec du rejet : Anomalie ID {} non trouvée ou déjà traitée.", anomalieId);
            return Optional.empty();
        }

        Anomalie anomalieARejeter = anomalieOpt.get();
        anomalieARejeter.setStatut(StatutAnomalie.REJETEE);
        anomalieARejeter.setCommentaireValidation(commentaire);
        anomalieARejeter.setDateResolution(LocalDateTime.now());

        Anomalie anomalieSauvegardee = anomalieRepository.save(anomalieARejeter);
        logger.info("Anomalie ID {} rejetée avec succès.", anomalieId);
        
        return Optional.of(anomalieSauvegardee);
    }

    /**
     * Corrige manuellement une anomalie d'omission en créant un nouveau pointage
     * et en passant l'anomalie au statut RESOLUE.
     * @param anomalieId L'ID de l'anomalie à corriger.
     * @param heureCorrigeeStr L'heure du pointage manquant, au format "HH:mm".
     * @param commentaire Le commentaire du manager.
     * @return L'anomalie mise à jour.
     */
    @Transactional
    public Optional<Anomalie> corrigerManuellement(Long anomalieId, String heureCorrigeeStr, String commentaire) {
        logger.info("Tentative de correction manuelle pour l'anomalie ID {} avec l'heure {}", anomalieId, heureCorrigeeStr);
        Optional<Anomalie> anomalieOpt = anomalieRepository.findById(anomalieId);

        if (anomalieOpt.isEmpty() || anomalieOpt.get().getStatut() != StatutAnomalie.EN_ATTENTE) {
            logger.warn("Correction manuelle impossible : anomalie {} non trouvée ou déjà traitée.", anomalieId);
            return Optional.empty();
        }

        Anomalie anomalie = anomalieOpt.get();
        
        if (anomalie.getTypeAnomalie() != TypeAnomalie.OMISSION_POINTAGE) {
            logger.warn("La correction manuelle avec ajout de pointage n'est applicable qu'aux OMISSION_POINTAGE. Type trouvé: {}", anomalie.getTypeAnomalie());
            return Optional.empty();
        }
        
        if (heureCorrigeeStr == null || heureCorrigeeStr.isBlank()) {
             logger.warn("Correction manuelle impossible pour anomalie {} : l'heure corrigée est vide.", anomalieId);
            return Optional.empty();
        }

        try {
            // Étape 1 : Créer le nouveau pointage
            LocalTime heureCorrigee = LocalTime.parse(heureCorrigeeStr);
            Pointage pointageCorrige = new Pointage();
            pointageCorrige.setBadgeEmploye(anomalie.getEmploye().getBadge());
            pointageCorrige.setDateMouvement(anomalie.getJourAnomalie().atTime(heureCorrigee));
            pointageCorrige.setCodePointeuse("CORR_MANUELLE_MANAGER");
            pointageRepository.save(pointageCorrige);

            // Étape 2 : Mettre à jour l'anomalie
            anomalie.setStatut(StatutAnomalie.RESOLUE);
            anomalie.setCommentaireValidation("Correction manuelle par manager : " + commentaire);
            anomalie.setDateResolution(LocalDateTime.now());
            anomalie.setSuggestion("Pointage manquant ajouté à " + heureCorrigeeStr);
            Anomalie anomalieSauvegardee = anomalieRepository.save(anomalie);
            
            logger.info("Anomalie ID {} résolue par correction manuelle. Pointage créé à {}.", anomalieId, heureCorrigeeStr);
            return Optional.of(anomalieSauvegardee);

        } catch (Exception e) {
            logger.error("Erreur de format d'heure ou autre lors de la correction manuelle pour l'anomalie ID {}.", anomalieId, e);
            return Optional.empty();
        }
    }
}