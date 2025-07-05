// FICHIER : AnomalieDto.java (Nouveau Fichier)
package ma.computime.anomalydetector.dto;

import lombok.Data;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.entity.TypeAnomalie;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AnomalieDto {
    private Long id;
    private LocalDate jourAnomalie;
    private TypeAnomalie typeAnomalie;
    private String message;
    private StatutAnomalie statut;
    private String suggestion;
    private LocalDateTime dateCreation;
    private String commentaireValidation;
    
    // Au lieu d'un objet Employe complet, on utilise notre DTO simple.
    private EmployeDto employe; 
}