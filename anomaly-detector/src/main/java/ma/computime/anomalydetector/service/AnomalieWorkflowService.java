// FICHIER : AnomalieWorkflowService.java (Nouveau Fichier)

package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AnomalieWorkflowService {

    @Autowired
    private AnomalieRepository anomalieRepository;

    /**
     * Valide une anomalie en changeant son statut à VALIDEE.
     * @param anomalieId L'ID de l'anomalie à valider.
     * @param commentaire Un commentaire optionnel de la part du validateur.
     * @return L'anomalie mise à jour si elle a été trouvée et était en attente.
     */
    public Optional<Anomalie> validerAnomalie(Long anomalieId, String commentaire) {
        // 1. On cherche l'anomalie dans la base de données par son ID.
        Optional<Anomalie> anomalieOpt = anomalieRepository.findById(anomalieId);

        // 2. Si on la trouve ET qu'elle est bien en attente...
        if (anomalieOpt.isPresent() && anomalieOpt.get().getStatut() == StatutAnomalie.EN_ATTENTE) {
            Anomalie anomalieAValider = anomalieOpt.get();
            // 3. On change son statut.
            anomalieAValider.setStatut(StatutAnomalie.VALIDEE);
            anomalieAValider.setCommentaireValidation(commentaire); // On ajoute le commentaire
            // 4. On sauvegarde les changements dans la base.
            Anomalie anomalieSauvegardee = anomalieRepository.save(anomalieAValider);
            return Optional.of(anomalieSauvegardee);
        }

        // 5. Si l'anomalie n'est pas trouvée ou n'est pas en attente, on ne retourne rien.
        return Optional.empty();
    }

    /**
     * Rejette une anomalie en changeant son statut à REJETEE.
     * @param anomalieId L'ID de l'anomalie à rejeter.
     * @param commentaire Un commentaire optionnel expliquant le rejet.
     * @return L'anomalie mise à jour si elle a été trouvée et était en attente.
     */
    public Optional<Anomalie> rejeterAnomalie(Long anomalieId, String commentaire) {
        Optional<Anomalie> anomalieOpt = anomalieRepository.findById(anomalieId);

        if (anomalieOpt.isPresent() && anomalieOpt.get().getStatut() == StatutAnomalie.EN_ATTENTE) {
            Anomalie anomalieARejeter = anomalieOpt.get();
            anomalieARejeter.setStatut(StatutAnomalie.REJETEE);
            anomalieARejeter.setCommentaireValidation(commentaire);
            Anomalie anomalieSauvegardee = anomalieRepository.save(anomalieARejeter);
            return Optional.of(anomalieSauvegardee);
        }

        return Optional.empty();
    }
}