// Emplacement : src/main/java/ma/computime/anomalydetector/controller/AnomalieController.java
package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.dto.AnomalieDto;
import ma.computime.anomalydetector.dto.AnomalieMapper;
import ma.computime.anomalydetector.dto.CorrectionPayload;
import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.service.AnomalieDetectionService;
import ma.computime.anomalydetector.service.AnomalieWorkflowService;
import ma.computime.anomalydetector.service.SuggestionActionService;
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

    @Autowired private AnomalieDetectionService anomalieDetectionService;
    @Autowired private AnomalieWorkflowService anomalieWorkflowService;
    @Autowired private SuggestionActionService suggestionActionService;
    @Autowired private AnomalieRepository anomalieRepository;

    @PostMapping("/detecter/{jour}")
    public ResponseEntity<String> declencherDetectionAnomalies(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour) {
        anomalieDetectionService.lancerDetectionPourTous(jour);
        return ResponseEntity.ok("Détection des anomalies lancée avec succès pour le " + jour);
    }

    // --- VUES POUR LE ROLE RH ---

    @GetMapping("/rh/en-attente")
    public List<AnomalieDto> getAnomaliesEnAttentePourRH() {
        List<Anomalie> anomaliesEnAttente = anomalieRepository.findByStatut(StatutAnomalie.EN_ATTENTE);
        return anomaliesEnAttente.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/rh/historique")
    public List<AnomalieDto> getHistoriqueCompletPourRH() {
        List<Anomalie> toutesLesAnomalies = anomalieRepository.findAll();
        return toutesLesAnomalies.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }
    
    // --- VUES POUR LE ROLE MANAGER ---

    @GetMapping("/manager/{managerId}/en-attente")
    public List<AnomalieDto> getAnomaliesEnAttentePourManager(@PathVariable Integer managerId) {
        List<Anomalie> anomalies = anomalieRepository.findByManagerAssigneIdAndStatut(managerId, StatutAnomalie.EN_ATTENTE);
        return anomalies.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }

    // --- ACTIONS DU MANAGER ---

    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerAnomalie(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";
        Optional<Anomalie> resultat = anomalieWorkflowService.validerAnomalie(id, commentaire);
        
        // --- DÉBUT DE LA CORRECTION ---
        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Anomalie non trouvée ou statut invalide pour l'ID : " + id);
        }
        // --- FIN DE LA CORRECTION ---
    }

    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterAnomalie(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";
        Optional<Anomalie> resultat = anomalieWorkflowService.rejeterAnomalie(id, commentaire);
        
        // --- DÉBUT DE LA CORRECTION ---
        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Anomalie non trouvée ou statut invalide pour l'ID : " + id);
        }
        // --- FIN DE LA CORRECTION ---
    }
    
    @PostMapping("/{id}/accepter-suggestion")
    public ResponseEntity<?> accepterSuggestion(@PathVariable Long id) {
        Optional<Anomalie> resultat = suggestionActionService.accepterSuggestion(id);
        
        // --- DÉBUT DE LA CORRECTION ---
        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible d'appliquer la suggestion pour l'anomalie ID : " + id);
        }
        // --- FIN DE LA CORRECTION ---
    }
    
    @PostMapping("/{id}/corriger-manuellement")
    public ResponseEntity<?> corrigerAnomalieManuellement(
            @PathVariable Long id,
            @RequestBody CorrectionPayload payload) {
        
        Optional<Anomalie> resultat = anomalieWorkflowService.corrigerManuellement(id, payload.getHeureCorrigee(), payload.getCommentaire());
        
        // --- DÉBUT DE LA CORRECTION ---
        if (resultat.isPresent()) {
            return ResponseEntity.ok(AnomalieMapper.toAnomalieDto(resultat.get()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible de corriger l'anomalie. Vérifiez l'ID, le statut, le type (OMISSION_POINTAGE) et le format de l'heure (HH:mm).");
        }
        // --- FIN DE LA CORRECTION ---
    }
}