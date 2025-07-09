// Dans ma/computime/anomalydetector/dto/IaSuggestionResponse.java
package ma.computime.anomalydetector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
// Cette annotation est importante : elle dit à Java d'ignorer les champs
// qu'il ne connaîtrait pas dans le JSON, ce qui évite des erreurs.
@JsonIgnoreProperties(ignoreUnknown = true)
public class IaSuggestionResponse {
    private String decision;
    private String confiance;
}