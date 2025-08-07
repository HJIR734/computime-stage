// Dans ma/computime/anomalydetector/scheduler/TachesPlanifiees.java
package ma.computime.anomalydetector.scheduler;

import ma.computime.anomalydetector.service.AnomalieDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component // Indique à Spring que c'est un composant à gérer
public class TachesPlanifiees {

    private static final Logger logger = LoggerFactory.getLogger(TachesPlanifiees.class);

    @Autowired
    private AnomalieDetectionService anomalieDetectionService;

    /**
     * Cette méthode s'exécute automatiquement tous les jours à 2h00 du matin.
     * La syntaxe "cron" signifie :
     * "0"  - seconde
     * "0"  - minute
     * "2"  - heure
     * "*"  - n'importe quel jour du mois
     * "*"  - n'importe quel mois
     * "?"  - n'importe quel jour de la semaine
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void lancerDetectionQuotidienne() {
        
        LocalDate jourADetecter = LocalDate.now().minusDays(1);
        
        logger.info("--- [SCHEDULER] : Lancement de la tâche de détection automatique pour la journée du {} ---", jourADetecter);
        
        try {
            
            anomalieDetectionService.lancerDetectionPourTous(jourADetecter);
            logger.info("--- [SCHEDULER] : Tâche de détection automatique terminée avec succès pour le {} ---", jourADetecter);
        } catch (Exception e) {
            logger.error("--- [SCHEDULER] : Une erreur est survenue lors de la tâche de détection automatique.", e);
        }
    }
}