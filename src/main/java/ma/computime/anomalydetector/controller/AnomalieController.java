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

    @GetMapping("/manager/{managerId}/en-attente/count")
    public long getNombreAnomaliesPourManager(@PathVariable Integer managerId) {
        return anomalieRepository.countByManagerResponsableIdAndStatut(managerId, StatutAnomalie.EN_ATTENTE);
    }

    @GetMapping("/manager/{managerId}/en-attente")
    public List<AnomalieDto> getAnomaliesPourManager(@PathVariable Integer managerId) {
        List<Anomalie> anomalies = anomalieRepository.findByManagerIdAndStatutWithEmploye(managerId, StatutAnomalie.EN_ATTENTE);
        return anomalies.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/accepter-suggestion")
    public ResponseEntity<AnomalieDto> accepterSuggestion(@PathVariable Long id) {
        Optional<Anomalie> resultat = suggestionActionService.accepterSuggestion(id);
        return resultat.map(anomalie -> ResponseEntity.ok(AnomalieMapper.toAnomalieDto(anomalie)))
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
    
    @PostMapping("/{id}/valider-manuellement")
    public ResponseEntity<AnomalieDto> validerAnomalieManuellement(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";
        Optional<Anomalie> resultat = anomalieWorkflowService.validerAnomalie(id, commentaire);
        return resultat.map(anomalie -> ResponseEntity.ok(AnomalieMapper.toAnomalieDto(anomalie)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{id}/rejeter-manuellement")
    public ResponseEntity<AnomalieDto> rejeterAnomalieManuellement(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";
        Optional<Anomalie> resultat = anomalieWorkflowService.rejeterAnomalie(id, commentaire);
        return resultat.map(anomalie -> ResponseEntity.ok(AnomalieMapper.toAnomalieDto(anomalie)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    
    @PostMapping("/{id}/corriger-manuellement")
    public ResponseEntity<AnomalieDto> corrigerAnomalieManuellement(
            @PathVariable Long id,
            @RequestBody CorrectionPayload payload) {
        Optional<Anomalie> resultat = anomalieWorkflowService.corrigerManuellement(id, payload.getHeureCorrigee(), payload.getCommentaire());
        return resultat.map(anomalie -> ResponseEntity.ok(AnomalieMapper.toAnomalieDto(anomalie)))
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
}