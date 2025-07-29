// Dans dto/PredictionEmploye.java
package ma.computime.anomalydetector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PredictionEmploye {
    private Integer employeId;
    private String nomComplet;
    private List<PredictionJour> predictions;
}