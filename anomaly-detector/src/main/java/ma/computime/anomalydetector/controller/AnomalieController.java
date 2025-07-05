// Emplacement : src/main/java/ma/computime/anomalydetector/controller/AnomalieController.java
package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.dto.AnomalieDto;
import ma.computime.anomalydetector.dto.AnomalieMapper;
import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.service.AnomalieDetectionService;
import ma.computime.anomalydetector.service.AnomalieWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalieController {

    // --- INJECTIONS DE DÉPENDANCES ---
    // On garde toutes les dépendances nécessaires des deux anciens controllers.
    @Autowired
    private AnomalieDetectionService anomalieDetectionService;

    @Autowired
    private AnomalieWorkflowService anomalieWorkflowService;

    @Autowired
    private AnomalieRepository anomalieRepository;

    // --- PARTIE DÉTECTION ---

    /**
     * DÉCLENCHE le scan de détection des anomalies pour une journée spécifique.
     * Cette méthode ne renvoie qu'un message de succès.
     */
    @PostMapping("/detecter/{jour}")
    public ResponseEntity<String> declencherDetectionAnomalies(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour) {
        anomalieDetectionService.lancerDetectionPourTous(jour);
        return ResponseEntity.ok("Détection des anomalies lancée avec succès pour le " + jour);
    }

    // --- PARTIE CONSULTATION ---

    /**
     * CONSULTE la liste des anomalies qui sont actuellement en attente de validation.
     * Renvoie une liste d'AnomalieDto pour une réponse API propre et concise.
     */
    @GetMapping("/en-attente")
    public List<AnomalieDto> getAnomaliesEnAttente() {
        List<Anomalie> anomaliesEnAttente = anomalieRepository.findByStatut(StatutAnomalie.EN_ATTENTE);
        return anomaliesEnAttente.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }

    /**
     * CONSULTE l'historique de toutes les anomalies, quel que soit leur statut.
     * Renvoie une liste d'AnomalieDto.
     */
    @GetMapping("/historique")
    public List<AnomalieDto> getToutesLesAnomalies() {
        List<Anomalie> toutesLesAnomalies = anomalieRepository.findAll();
        return toutesLesAnomalies.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }

    // --- PARTIE WORKFLOW (VALIDATION / REJET) ---
    // On utilise la version la plus robuste et la plus claire (avec if/else).

    /**
     * Endpoint pour VALIDER une anomalie.
     * Attend un ID dans l'URL et un corps JSON avec un commentaire.
     * Exemple de corps JSON : { "commentaire": "Validé par le manager." }
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerAnomalie(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";

        Optional<Anomalie> resultat = anomalieWorkflowService.validerAnomalie(id, commentaire);

        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            String messageErreur = "Anomalie non trouvée ou statut invalide pour l'ID : " + id;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageErreur);
        }
    }

    /**
     * Endpoint pour REJETER une anomalie.
     * Attend un ID dans l'URL et un corps JSON avec un commentaire.
     * Exemple de corps JSON : { "commentaire": "Ceci n'est pas une anomalie." }
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterAnomalie(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";

        Optional<Anomalie> resultat = anomalieWorkflowService.rejeterAnomalie(id, commentaire);

        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            String messageErreur = "Anomalie non trouvée ou statut invalide pour l'ID : " + id;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageErreur);
        }
    }
}