// Emplacement : src/main/java/ma/computime/anomalydetector/dto/AnomalieInfo.java
package ma.computime.anomalydetector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class AnomalieInfo {
    private String typeAnomalie;
    private String message;
    private String badgeEmploye;
    private String nomEmploye;
    private String suggestion;
}