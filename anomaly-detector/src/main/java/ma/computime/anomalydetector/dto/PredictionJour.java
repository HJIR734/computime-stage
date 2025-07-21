// Dans dto/PredictionJour.java
package ma.computime.anomalydetector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PredictionJour {
    private LocalDate date;
    private double probabiliteAbsence;
    private String niveauRisque; // "Faible", "Moyen", "Élevé"
}