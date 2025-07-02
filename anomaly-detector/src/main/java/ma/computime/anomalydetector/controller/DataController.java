// Emplacement : src/main/java/ma/computime/anomalydetector/controller/DataController.java
package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.dto.AnomalieInfo; // <-- NOUVEL IMPORT
import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.Pointage;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import ma.computime.anomalydetector.service.AnomalieDetectionService; // <-- NOUVEL IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // <-- NOUVEL IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate; // <-- NOUVEL IMPORT
import java.util.List;

@RestController
@RequestMapping("/api")
public class DataController {

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private PointageRepository pointageRepository;

    // INJECTION DE NOTRE NOUVEAU SERVICE
    @Autowired
    private AnomalieDetectionService anomalieDetectionService;

    // --- Endpoints existants pour la lecture des données brutes ---

    @GetMapping("/employes")
    public List<Employe> getAllEmployes() {
        return employeRepository.findAll();
    }

    @GetMapping("/employes/{badge}")
    public ResponseEntity<Employe> getEmployeByBadge(@PathVariable String badge) {
        return employeRepository.findByBadge(badge)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/employes/{badge}/pointages")
    public List<Pointage> getPointagesByEmployeBadge(@PathVariable String badge) {
        return pointageRepository.findByBadgeEmploye(badge);
    }
    
    // --- NOUVEL ENDPOINT POUR LA DÉTECTION D'ANOMALIES ---

    /**
     * Lance la détection des anomalies pour une journée spécifique.
     * L'URL doit être au format YYYY-MM-DD.
     * Exemple : GET http://localhost:8080/api/anomalies/2023-01-20
     * @param jour La date pour laquelle effectuer la détection.
     * @return Une liste des anomalies trouvées.
     */
    @GetMapping("/anomalies/{jour}")
    public List<AnomalieInfo> detecterAnomalies(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate jour) {
        return anomalieDetectionService.detecterAnomaliesPourTous(jour);
    }
}