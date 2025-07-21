// Dans dto/PredictionAbsenceResponse.java
package ma.computime.anomalydetector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PredictionAbsenceResponse {

    @JsonProperty("probabilite_absence")
    private double probabiliteAbsence;
}