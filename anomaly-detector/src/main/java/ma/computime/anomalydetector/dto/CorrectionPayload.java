// Emplacement : src/main/java/ma/computime/anomalydetector/dto/CorrectionPayload.java
package ma.computime.anomalydetector.dto;

import lombok.Data;

@Data
public class CorrectionPayload {
    // Commentaire optionnel du manager
    private String commentaire;
    
    // L'heure corrigée fournie par le manager (format "HH:mm")
    // C'est spécifique à l'omission de pointage pour l'instant.
    private String heureCorrigee; 
}