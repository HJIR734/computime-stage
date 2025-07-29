// Emplacement : src/main/java/ma/computime/anomalydetector/controller/DashboardController.java
package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.dto.AnomalieDto;
import ma.computime.anomalydetector.dto.AnomalieMapper;
import ma.computime.anomalydetector.dto.PredictionEmploye;
import ma.computime.anomalydetector.dto.PredictionJour;
import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.Noeud;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.NoeudRepository;
import ma.computime.anomalydetector.service.AnomalieDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired private AnomalieRepository anomalieRepository;
    @Autowired private EmployeRepository employeRepository;
    @Autowired private AnomalieDetectionService anomalieService;
    @Autowired private NoeudRepository noeudRepository;

    // --- Tes méthodes existantes (on ne les touche pas) ---
    @GetMapping("/noeud/{noeudId}")
    public String getDashboardPourNoeud(@PathVariable Integer noeudId, Model model) {
        // ... Ton code existant pour cette méthode
        var anomaliesEnAttente = anomalieRepository.findByNoeudIdAndStatutWithEmploye(noeudId, StatutAnomalie.EN_ATTENTE).stream().map(AnomalieMapper::toAnomalieDto).collect(Collectors.toList());
        var historiqueRecent = anomalieRepository.findByNoeudIdAndStatutNotWithEmploye(noeudId, StatutAnomalie.EN_ATTENTE).stream().map(AnomalieMapper::toAnomalieDto).collect(Collectors.toList());
        model.addAttribute("titre", "Anomalies du Service (Noeud " + noeudId + ")");
        model.addAttribute("anomalies", anomaliesEnAttente);
        model.addAttribute("historique", historiqueRecent);
        model.addAttribute("managerId", 0);
        return "dashboard";
    }
    
    @GetMapping("/manager/{managerId}")
    public String getDashboardPourManager(@PathVariable Integer managerId, Model model) {
        // ... Ton code existant pour cette méthode
        var anomaliesEnAttente = anomalieRepository.findByManagerIdAndStatutWithEmploye(managerId, StatutAnomalie.EN_ATTENTE).stream().map(AnomalieMapper::toAnomalieDto).collect(Collectors.toList());
        var historiqueRecent = anomalieRepository.findByManagerIdAndStatutNotWithEmploye(managerId, StatutAnomalie.EN_ATTENTE).stream().map(AnomalieMapper::toAnomalieDto).collect(Collectors.toList());
        model.addAttribute("titre", "Mes Anomalies à Traiter");
        model.addAttribute("anomalies", anomaliesEnAttente);
        model.addAttribute("historique", historiqueRecent);
        model.addAttribute("managerId", managerId);
        return "dashboard";
    }

    // ====================================================================
    // === NOUVEL ENDPOINT AVEC LA BONNE LOGIQUE HIÉRARCHIQUE ===
    // ====================================================================
    
    @GetMapping("/manager/{managerId}/predictions")
    @ResponseBody
    public ResponseEntity<List<PredictionEmploye>> getPredictionsPourManager(@PathVariable Integer managerId) {
        
        Employe manager = employeRepository.findById(managerId).orElse(null);

        if (manager == null || manager.getNoeud() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        // La logique correcte : trouver les employés par noeud
        List<Employe> employesDuManager = employeRepository.findByNoeud(manager.getNoeud());

        LocalDate dateDebut = LocalDate.now();
        List<LocalDate> periode = dateDebut.datesUntil(dateDebut.plusDays(7)).collect(Collectors.toList());
        
        List<PredictionEmploye> resultatFinal = new ArrayList<>();

        for (Employe employe : employesDuManager) {
            if (employe.getId().equals(managerId)) continue;

            List<PredictionJour> predictionsPourCetEmploye = new ArrayList<>();
            for (LocalDate jour : periode) {
                double probabilite = anomalieService.getAbsencePrediction(jour, employe);
                String niveauRisque = "Faible";
                if (probabilite > 0.75) niveauRisque = "Élevé";
                else if (probabilite > 0.40) niveauRisque = "Moyen";

                predictionsPourCetEmploye.add(new PredictionJour(jour, probabilite, niveauRisque));
            }
            resultatFinal.add(new PredictionEmploye(
                employe.getId(), 
                employe.getPrenom() + " " + employe.getNom(), 
                predictionsPourCetEmploye
            ));
        }
        
        return ResponseEntity.ok(resultatFinal);
    }
}