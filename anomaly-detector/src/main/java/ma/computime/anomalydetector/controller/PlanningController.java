// Dans controller/PlanningController.java
package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.dto.PredictionEmploye;
import ma.computime.anomalydetector.service.PlanningPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {

    @Autowired
    private PlanningPredictionService planningPredictionService;

    @GetMapping("/predictions/manager/{managerId}")
    public List<PredictionEmploye> getAbsencePredictions(
            @PathVariable Integer managerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        
        return planningPredictionService.getPredictionsPourEquipe(managerId, dateDebut, dateFin);
    }
}