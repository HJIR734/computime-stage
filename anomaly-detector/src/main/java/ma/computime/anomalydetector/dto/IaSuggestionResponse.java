// Dans ma/computime/anomalydetector/dto/IaSuggestionResponse.java
package ma.computime.anomalydetector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IaSuggestionResponse {
    private String decision;
    private String confiance;
    private List<String> justification;
}