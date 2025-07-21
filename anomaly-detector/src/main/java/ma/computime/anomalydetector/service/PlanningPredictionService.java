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
    private AnomalieDetectionService anomalieDetectionService; // Pour réutiliser la méthode d'appel à l'IA

    public List<PredictionEmploye> getPredictionsPourEquipe(Integer managerId, LocalDate dateDebut, LocalDate dateFin) {
        
        // 1. Trouver les employés qui reportent à ce manager (subordonnés directs)
        // Pour l'instant, on prend tous les employés pour le test. On affinera plus tard si besoin.
        List<Employe> employes = employeRepository.findAll(); // Simplifié pour le moment

        List<PredictionEmploye> resultatFinal = new ArrayList<>();

        // 2. Pour chaque employé, générer les prédictions
        for (Employe employe : employes) {
            List<PredictionJour> predictionsPourEmploye = new ArrayList<>();
            
            // 3. Boucler sur chaque jour de la période demandée
            for (LocalDate jour = dateDebut; !jour.isAfter(dateFin); jour = jour.plusDays(1)) {
                
                // 4. Appeler notre méthode existante pour obtenir la probabilité de l'IA
                double probabilite = anomalieDetectionService.getAbsencePrediction(jour, employe);

                // 5. Transformer la probabilité en un niveau de risque
                String risque = determinerNiveauRisque(probabilite);
                
                // 6. Créer l'objet de prédiction pour ce jour
                predictionsPourEmploye.add(new PredictionJour(jour, probabilite, risque));
            }
            
            // 7. Assembler le résultat pour cet employé
            resultatFinal.add(new PredictionEmploye(
                employe.getId(), 
                employe.getPrenom() + " " + employe.getNom(), 
                predictionsPourEmploye
            ));
        }

        return resultatFinal;
    }
    
    private String determinerNiveauRisque(double probabilite) {
        if (probabilite < 0) return "Erreur"; // Cas où l'API IA n'a pas répondu
        if (probabilite >= 0.15) return "Élevé";
        if (probabilite >= 0.05) return "Moyen";
        return "Faible";
    }
}