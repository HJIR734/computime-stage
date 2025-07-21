// Emplacement : src/main/java/ma/computime/anomalydetector/controller/AnomalieController.java
package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.dto.AnomalieDto;
import ma.computime.anomalydetector.dto.AnomalieMapper;
import ma.computime.anomalydetector.dto.CorrectionPayload;
import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.service.AnomalieDetectionService;
import ma.computime.anomalydetector.service.AnomalieWorkflowService;
import ma.computime.anomalydetector.service.SuggestionActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/anomalies")
public class AnomalieController {

    @Autowired private AnomalieDetectionService anomalieDetectionService;
    @Autowired private AnomalieWorkflowService anomalieWorkflowService;
    @Autowired private SuggestionActionService suggestionActionService;
    @Autowired private AnomalieRepository anomalieRepository;
    @Autowired private EmployeRepository employeRepository;

    // ====================================================================
    // ENDPOINTS DE DÉCLENCHEMENT ET DE CONSULTATION
    // ====================================================================

    @PostMapping("/detecter/{jour}")
    public ResponseEntity<String> declencherDetectionAnomalies(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour) {
        anomalieDetectionService.lancerDetectionPourTous(jour);
        return ResponseEntity.ok("Détection des anomalies lancée avec succès pour le " + jour);
    }

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
    
    @GetMapping("/noeud/{noeudId}/en-attente")
    public List<AnomalieDto> getAnomaliesEnAttentePourNoeud(@PathVariable Integer noeudId) {
        List<Anomalie> anomalies = anomalieRepository.findByNoeudConcerneIdAndStatut(noeudId, StatutAnomalie.EN_ATTENTE);
        return anomalies.stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
    }

    // Dans AnomalieController.java

// ... (après la méthode getAnomaliesEnAttentePourNoeud)

// ====================================================================
// ENDPOINTS POUR LE DASHBOARD PERSONNEL DU MANAGER (VERSION CORRIGÉE)
// ====================================================================

/**
 * Renvoie le NOMBRE d'anomalies en attente assignées à un manager spécifique.
 * C'est pour la "notification" dans le menu (ex: "Anomalies à traiter (5)").
 * URL de test : /api/anomalies/manager/465/en-attente/count
 */
@GetMapping("/manager/{managerId}/en-attente/count")
public long getNombreAnomaliesPourManager(@PathVariable Integer managerId) {
    // ON UTILISE DIRECTEMENT TA MÉTHODE EXISTANTE
    // Pas besoin de chercher l'employé d'abord. C'est plus efficace.
    return anomalieRepository.countByManagerResponsableIdAndStatut(managerId, StatutAnomalie.EN_ATTENTE);
}

/**
 * Renvoie la LISTE des anomalies en attente assignées à un manager spécifique.
 * C'est pour afficher la page détaillée que le manager doit traiter.
 * URL de test : /api/anomalies/manager/465/en-attente
 */
@GetMapping("/manager/{managerId}/en-attente")
public List<AnomalieDto> getAnomaliesPourManager(@PathVariable Integer managerId) {
    // ON UTILISE DIRECTEMENT TA MÉTHODE EXISTANTE
    List<Anomalie> anomalies = anomalieRepository.findByManagerResponsableIdAndStatut(managerId, StatutAnomalie.EN_ATTENTE);
    
    // On convertit la liste en DTOs
    return anomalies.stream()
            .map(AnomalieMapper::toAnomalieDto)
            .collect(Collectors.toList());
}


// (Ici commence la section "ENDPOINTS D'ACTION DU MANAGER...")


// (Ici commence la section "ENDPOINTS D'ACTION DU MANAGER...")
    // ====================================================================
    // ENDPOINTS D'ACTION DU MANAGER SUR LES SUGGESTIONS DE L'IA
    // ====================================================================
    
    /**
     * Accepte la suggestion de l'IA pour une anomalie.
     * L'action dépend du type d'anomalie (ex: ajoute un pointage pour une omission, valide un retard, etc.).
     */
    @PostMapping("/{id}/accepter-suggestion")
    public ResponseEntity<AnomalieDto> accepterSuggestion(@PathVariable Long id) {
        Optional<Anomalie> resultat = suggestionActionService.accepterSuggestion(id);
        
        return resultat.map(anomalie -> ResponseEntity.ok(AnomalieMapper.toAnomalieDto(anomalie)))
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    

    /**
     * Valide manuellement une anomalie, sans tenir compte de la suggestion IA.
     * C'est une action de forçage par le RH ou le manager.
     */
    @PostMapping("/{id}/valider-manuellement")
    public ResponseEntity<AnomalieDto> validerAnomalieManuellement(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
    String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";
        Optional<Anomalie> resultat = anomalieWorkflowService.validerAnomalie(id, commentaire);
        
        return resultat.map(anomalie -> ResponseEntity.ok(AnomalieMapper.toAnomalieDto(anomalie)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Rejette manuellement une anomalie, sans tenir compte de la suggestion IA.
     */
    @PostMapping("/{id}/rejeter-manuellement")
    public ResponseEntity<AnomalieDto> rejeterAnomalieManuellement(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
    String commentaire = (payload != null) ? payload.getOrDefault("commentaire", "") : "";
        Optional<Anomalie> resultat = anomalieWorkflowService.rejeterAnomalie(id, commentaire);
        
        return resultat.map(anomalie -> ResponseEntity.ok(AnomalieMapper.toAnomalieDto(anomalie)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    
    /**
     * Permet de corriger manuellement une omission de pointage en fournissant l'heure.
     */
    @PostMapping("/{id}/corriger-manuellement")
    public ResponseEntity<AnomalieDto> corrigerAnomalieManuellement(
            @PathVariable Long id,
            @RequestBody CorrectionPayload payload) {
        
        Optional<Anomalie> resultat = anomalieWorkflowService.corrigerManuellement(id, payload.getHeureCorrigee(), payload.getCommentaire());
        
        return resultat.map(anomalie -> ResponseEntity.ok(AnomalieMapper.toAnomalieDto(anomalie)))
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
}