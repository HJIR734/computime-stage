// Emplacement : ma/computime/anomalydetector/dto/AnomalieDto.java
package ma.computime.anomalydetector.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AnomalieDto {
    private Long id;
    private String nomEmploye; 
    private String badgeEmploye;
    private String jourAnomalie; 
    private String typeAnomalie;
    private String message;
    private String statut;
    private String commentaireValidation;
    private Long dureeEnMinutes;
    private String suggestion; 
    private LocalTime valeurSuggestion;
}