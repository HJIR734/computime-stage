// Emplacement : src/main/java/ma/computime/anomalydetector/service/AnomalieWorkflowService.java
package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORT IMPORTANT

import java.time.LocalDateTime; // <-- IMPORT IMPORTANT
import java.util.Optional;

@Service
public class AnomalieWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(AnomalieWorkflowService.class);

    @Autowired
    private AnomalieRepository anomalieRepository;

    /**
     * Valide une anomalie en changeant son statut à VALIDEE.
     * Cette opération est transactionnelle : soit tout réussit, soit tout est annulé.
     * @param anomalieId L'ID de l'anomalie à valider.
     * @param commentaire Un commentaire optionnel de la part du validateur.
     * @return L'anomalie mise à jour si elle a été trouvée et était en attente.
     */
    @Transactional // Garantit la cohérence des données
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
        anomalieAValider.setDateResolution(LocalDateTime.now()); // On enregistre quand elle a été traitée

        Anomalie anomalieSauvegardee = anomalieRepository.save(anomalieAValider);
        logger.info("Anomalie ID {} validée avec succès.", anomalieId);
        
        return Optional.of(anomalieSauvegardee);
    }

    /**
     * Rejette une anomalie en changeant son statut à REJETEE.
     * @param anomalieId L'ID de l'anomalie à rejeter.
     * @param commentaire Un commentaire optionnel expliquant le rejet.
     * @return L'anomalie mise à jour si elle a été trouvée et était en attente.
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
}