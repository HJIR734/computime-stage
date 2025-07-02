// Emplacement : src/main/java/ma/computime/anomalydetector/dto/AnomalieInfo.java
package ma.computime.anomalydetector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un simple objet pour transporter les informations sur une anomalie détectée.
 */
@Data // Lombok: génère getters, setters, toString...
@NoArgsConstructor // Lombok: génère un constructeur sans arguments
@AllArgsConstructor // Lombok: génère un constructeur avec tous les arguments
public class AnomalieInfo {
    private String typeAnomalie;
    private String message;
    private String badgeEmploye;
    private String nomEmploye;
}