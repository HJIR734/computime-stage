// Dans service/PlanningPredictionService.java
package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.dto.PredictionEmploye;
import ma.computime.anomalydetector.dto.PredictionJour;
import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanningPredictionService {

    @Autowired
    private EmployeRepository employeRepository;
    
    @Autowired
    private AnomalieDetectionService anomalieDetectionService; 

    public List<PredictionEmploye> getPredictionsPourEquipe(Integer managerId, LocalDate dateDebut, LocalDate dateFin) {
        
        
        List<Employe> employes = employeRepository.findAll(); 

        List<PredictionEmploye> resultatFinal = new ArrayList<>();

        
        for (Employe employe : employes) {
            List<PredictionJour> predictionsPourEmploye = new ArrayList<>();
            
            
            for (LocalDate jour = dateDebut; !jour.isAfter(dateFin); jour = jour.plusDays(1)) {
                
                
                double probabilite = anomalieDetectionService.getAbsencePrediction(jour, employe)* 100.0;

                
                String risque = determinerNiveauRisque(probabilite);
                
                
                predictionsPourEmploye.add(new PredictionJour(jour, probabilite, risque));
            }
            
            
            resultatFinal.add(new PredictionEmploye(
                employe.getId(), 
                employe.getPrenom() + " " + employe.getNom(), 
                predictionsPourEmploye
            ));
        }

        return resultatFinal;
    }
    
    private String determinerNiveauRisque(double probabilite) {
        if (probabilite < 0) return "Erreur"; 
        if (probabilite >= 5.0) return "Élevé";
        if (probabilite >= 1.5) return "Moyen";
        return "Faible";
    }
}