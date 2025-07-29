// Dans ma/computime/anomalydetector/dto/ValidationRequest.java
package ma.computime.anomalydetector.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class ValidationRequest {
    private LocalTime heureCorrection; 
    private String commentaire; 
}