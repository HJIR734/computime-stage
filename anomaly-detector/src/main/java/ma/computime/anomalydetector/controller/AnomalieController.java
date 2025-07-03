// Emplacement : src/main/java/ma/computime/anomalydetector/controller/AnomalieController.java
package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.service.AnomalieDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/anomalies") // Toutes les URLs de ce contrôleur commenceront par /api/anomalies
public class AnomalieController {

    @Autowired
    private AnomalieDetectionService anomalieService;

    @Autowired
    private AnomalieRepository anomalieRepository;

    /**
     * Lance le scan de détection des anomalies pour une journée spécifique.
     * C'est une action qui modifie l'état du système (crée des anomalies),
     * donc on utilise une requête POST.
     *
     * @param jour La date au format YYYY-MM-DD.
     * @return Une réponse de confirmation.
     */
    @PostMapping("/detecter/{jour}")
    public ResponseEntity<String> declencherDetectionAnomalies(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour) {
        
        anomalieService.lancerDetectionPourTous(jour);
        return ResponseEntity.ok("Détection des anomalies lancée avec succès pour le " + jour);
    }

    /**
     * Récupère la liste de toutes les anomalies qui sont actuellement en attente de validation.
     * C'est le endpoint que le manager utilisera pour voir les tâches à traiter.
     *
     * @return Une liste d'objets Anomalie.
     */
    @GetMapping("/en-attente")
    public List<Anomalie> getAnomaliesEnAttente() {
        return anomalieRepository.findAll()
                .stream()
                .filter(anomalie -> anomalie.getStatut() == StatutAnomalie.EN_ATTENTE)
                .collect(Collectors.toList());
    }

    /**
     * Récupère la liste de toutes les anomalies, quel que soit leur statut.
     * Utile pour avoir un historique complet.
     *
     * @return Une liste de toutes les anomalies enregistrées.
     */
    @GetMapping("/historique")
    public List<Anomalie> getToutesLesAnomalies() {
        return anomalieRepository.findAll();
    }
    
    // ===================================================================
    // FUTURS ENDPOINTS POUR LE WORKFLOW DE VALIDATION
    // ===================================================================
    // Nous les implémenterons dans la prochaine étape.
    
    /*
    @PostMapping("/{id}/valider")
    public ResponseEntity<Anomalie> validerAnomalie(@PathVariable Long id) {
        // Logique pour valider une anomalie
        return null; 
    }

    @PostMapping("/{id}/rejeter")
    public ResponseEntity<Anomalie> rejeterAnomalie(@PathVariable Long id) {
        // Logique pour rejeter une anomalie
        return null;
    }
    */
    // ===================================================================
}