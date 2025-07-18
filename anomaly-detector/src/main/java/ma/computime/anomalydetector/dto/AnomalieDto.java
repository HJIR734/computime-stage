// Emplacement : ma/computime/anomalydetector/dto/AnomalieDto.java
package ma.computime.anomalydetector.dto;

import lombok.Data;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.entity.TypeAnomalie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime; // N'oublie pas cet import

@Data
public class AnomalieDto {
    private Long id;
    private LocalDate jourAnomalie;
    private TypeAnomalie typeAnomalie;
    private String message;
    private StatutAnomalie statut;
    private LocalDateTime dateCreation;
    private String commentaireValidation;
    
    // Au lieu d'un objet Employe complet, on utilise notre DTO simple.
    private EmployeDto employe;
    
    // Champ pour la durée, utile à afficher
    private Long dureeEnMinutes;
    
    // ====================================================================
    // === CHAMPS DE L'IA - AJOUTÉS POUR LE FRONTEND ===
    // ====================================================================
    
    /**
     * La décision principale de l'IA (ex: "ACCEPTER").
     */
    private String decisionIa;

    /**
     * La justification textuelle complète de l'IA.
     */
    private String justificationIa;

    /**
     * La valeur concrète suggérée par l'IA (ex: l'heure '08:30').
     */
    private LocalTime valeurSuggestion;

    // L'ancien champ 'suggestion' (String) est maintenant redondant
    // car 'justificationIa' est plus complet. On peut le commenter ou le supprimer.
    // private String suggestion; 
    private String suggestion;
}