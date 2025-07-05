// Dans ma/computime/anomalydetector/dto/ValidationRequest.java
package ma.computime.anomalydetector.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class ValidationRequest {
    // L'heure de correction proposée par le manager
    private LocalTime heureCorrection; 
    
    // Un commentaire optionnel du manager
    private String commentaire; 
}