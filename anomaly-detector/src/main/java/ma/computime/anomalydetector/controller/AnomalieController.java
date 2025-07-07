// Emplacement : src/main/java/ma/computime/anomalydetector/controller/AnomalieController.java
package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.dto.AnomalieDto;
import ma.computime.anomalydetector.dto.AnomalieMapper;
import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.service.AnomalieDetectionService;
import ma.computime.anomalydetector.service.AnomalieWorkflowService;
import ma.computime.anomalydetector.service.SuggestionActionService; // <-- NOUVEL IMPORT
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

    @Autowired
    private AnomalieDetectionService anomalieDetectionService;

    @Autowired
    private AnomalieWorkflowService anomalieWorkflowService;
    
    @Autowired
    private SuggestionActionService suggestionActionService; // <-- NOUVELLE INJECTION

    @Autowired
    private AnomalieRepository anomalieRepository;

    // --- PARTIE DÉTECTION ---

    @PostMapping("/detecter/{jour}")
    public ResponseEntity<String> declencherDetectionAnomalies(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour) {
        anomalieDetectionService.lancerDetectionPourTous(jour);
        return ResponseEntity.ok("Détection des anomalies lancée avec succès pour le " + jour);
    }

    // --- PARTIE CONSULTATION ---

    @GetMapping("/en-attente")
    public List<AnomalieDto> getAnomaliesEnAttente() {
        List<Anomalie> anomaliesEnAttente = anomalieRepository.findByStatut(StatutAnomalie.EN_ATTENTE);
        return anomaliesEnAttente.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/historique")
    public List<AnomalieDto> getToutesLesAnomalies() {
        List<Anomalie> toutesLesAnomalies = anomalieRepository.findAll();
        return toutesLesAnomalies.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }

    // --- PARTIE WORKFLOW (VALIDATION / REJET MANUEL) ---

    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerAnomalie(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";
        Optional<Anomalie> resultat = anomalieWorkflowService.validerAnomalie(id, commentaire);
        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Anomalie non trouvée ou statut invalide pour l'ID : " + id);
        }
    }

    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterAnomalie(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";
        Optional<Anomalie> resultat = anomalieWorkflowService.rejeterAnomalie(id, commentaire);
        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Anomalie non trouvée ou statut invalide pour l'ID : " + id);
        }
    }

    // --- NOUVEL ENDPOINT POUR ACCEPTER LA SUGGESTION IA ---
    /**
     * Accepte la suggestion de l'IA pour une anomalie spécifique,
     * ce qui crée un pointage et résout l'anomalie.
     * @param id L'ID de l'anomalie à traiter.
     * @return L'anomalie mise à jour avec le statut RESOLUE, ou une erreur 404/400.
     */
    @PostMapping("/{id}/accepter-suggestion")
    public ResponseEntity<?> accepterSuggestion(@PathVariable Long id) {
        Optional<Anomalie> resultat = suggestionActionService.accepterSuggestion(id);
        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            String messageErreur = "Impossible d'appliquer la suggestion pour l'anomalie ID : " + id + ". Vérifiez qu'elle existe, qu'elle est en attente et qu'elle a une suggestion d'heure.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageErreur);
        }
    }
}