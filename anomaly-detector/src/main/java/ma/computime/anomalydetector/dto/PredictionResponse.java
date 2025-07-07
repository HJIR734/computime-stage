// Dans ma/computime/anomalydetector/dto/PredictionResponse.java
package ma.computime.anomalydetector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PredictionResponse {

    @JsonProperty("suggestion_heure")
    private String suggestionHeure;
}