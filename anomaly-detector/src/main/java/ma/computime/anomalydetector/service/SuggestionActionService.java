// Emplacement : src/main/java/ma/computime/anomalydetector/service/SuggestionActionService.java
package ma.computime.anomalydetector.service;

// --- IMPORTS ---
import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.Pointage;
import ma.computime.anomalydetector.entity.StatutAnomalie; // <-- L'IMPORT QUI MANQUAIT
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Ce service gère les actions liées aux suggestions d'anomalies,
 * comme l'acceptation d'une suggestion de l'IA.
 */
@Service
public class SuggestionActionService {

    @Autowired
    private AnomalieRepository anomalieRepository;

    @Autowired
    private PointageRepository pointageRepository;

    /**
     * Accepte la suggestion pour une anomalie d'omission.
     * Cette méthode va créer un nouveau pointage basé sur la suggestion,
     * puis marquer l'anomalie comme résolue.
     * L'annotation @Transactional garantit que toutes ces opérations
     * sont effectuées en une seule transaction : si une étape échoue, tout est annulé.
     *
     * @param anomalieId L'ID de l'anomalie à traiter.
     * @return Un Optional contenant l'anomalie mise à jour si l'opération a réussi,
     *         sinon un Optional vide.
     */
    @Transactional
    public Optional<Anomalie> accepterSuggestion(Long anomalieId) {
        // 1. Trouver l'anomalie dans la base de données.
        Optional<Anomalie> anomalieOpt = anomalieRepository.findById(anomalieId);

        // 2. Vérifier les conditions : l'anomalie doit exister ET avoir une suggestion d'heure applicable.
        if (anomalieOpt.isEmpty() || anomalieOpt.get().getValeurSuggestion() == null) {
            return Optional.empty(); // On ne peut rien faire.
        }

        Anomalie anomalie = anomalieOpt.get();
        
        // Sécurité supplémentaire : ne rien faire si l'anomalie n'est pas en attente.
        if (anomalie.getStatut() != StatutAnomalie.EN_ATTENTE) {
            return Optional.empty();
        }

        // 3. Créer le nouveau pointage.
        Pointage nouveauPointage = new Pointage();
        nouveauPointage.setEmploye(anomalie.getEmploye());
        nouveauPointage.setBadgeEmploye(anomalie.getEmploye().getBadge());
        LocalDateTime dateHeurePointage = anomalie.getJourAnomalie().atTime(anomalie.getValeurSuggestion());
        nouveauPointage.setDateMouvement(dateHeurePointage);
        nouveauPointage.setCodePointeuse("CORRECTION_IA");

        // 4. Sauvegarder le nouveau pointage en base.
        pointageRepository.save(nouveauPointage);

        // 5. Mettre à jour l'anomalie.
        anomalie.setStatut(StatutAnomalie.RESOLUE);
        anomalie.setCommentaireValidation("Suggestion de l'IA acceptée et appliquée automatiquement.");
        anomalie.setDateResolution(LocalDateTime.now());
        
        Anomalie anomalieResolue = anomalieRepository.save(anomalie);

        // 6. Renvoyer l'anomalie mise à jour.
        return Optional.of(anomalieResolue);
    }
}