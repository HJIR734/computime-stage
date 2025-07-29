// Emplacement : src/main/java/ma/computime/anomalydetector/dto/CorrectionPayload.java
package ma.computime.anomalydetector.dto;

import lombok.Data;

@Data
public class CorrectionPayload {
    private String commentaire;
    private String heureCorrigee; 
}