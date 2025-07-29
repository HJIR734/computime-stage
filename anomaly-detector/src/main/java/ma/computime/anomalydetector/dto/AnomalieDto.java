// Emplacement : ma/computime/anomalydetector/dto/AnomalieDto.java
package ma.computime.anomalydetector.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AnomalieDto {
    private Long id;
    
    // --- Informations sur l'employé (APLATIES) ---
    private String nomEmploye; // Format "Prénom Nom"
    private String badgeEmploye;

    // --- Informations sur l'anomalie ---
    private String jourAnomalie; // On va le passer en String formaté (ex: "28/07/2025")
    private String typeAnomalie;
    private String message;
    private String statut;
    private String commentaireValidation;
    private Long dureeEnMinutes;
    
    // --- Informations de l'IA ---
    private String suggestion; // On garde un champ simple pour l'affichage
    private LocalTime valeurSuggestion; // Utile pour pré-remplir les champs de temps
}