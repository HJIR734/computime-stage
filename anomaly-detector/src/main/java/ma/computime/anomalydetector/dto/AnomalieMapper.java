// Emplacement : ma/computime/anomalydetector/dto/AnomalieMapper.java
package ma.computime.anomalydetector.dto;

import ma.computime.anomalydetector.entity.Anomalie;
import java.time.format.DateTimeFormatter;

public class AnomalieMapper {

    // On définit nos formats de date ici, une seule fois.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Convertit une entité Anomalie en notre DTO simple pour l'affichage
    public static AnomalieDto toAnomalieDto(Anomalie anomalie) {
        if (anomalie == null) {
            return null;
        }
        
        AnomalieDto dto = new AnomalieDto();
        
        // --- Remplissage des champs de l'anomalie ---
        dto.setId(anomalie.getId());
        dto.setMessage(anomalie.getMessage());
        dto.setSuggestion(anomalie.getSuggestion()); // Le champ de suggestion simple
        dto.setValeurSuggestion(anomalie.getValeurSuggestion()); // L'heure suggérée
        dto.setDureeEnMinutes(anomalie.getDureeEnMinutes());
        dto.setCommentaireValidation(anomalie.getCommentaireValidation());

        // --- Remplissage des champs formatés ---
        dto.setJourAnomalie(anomalie.getJourAnomalie().format(DATE_FORMATTER));
        dto.setTypeAnomalie(anomalie.getTypeAnomalie().toString());
        dto.setStatut(anomalie.getStatut().toString());

        // --- Remplissage des champs de l'employé (la partie importante) ---
        if (anomalie.getEmploye() != null) {
            // On construit le nom complet ici
            dto.setNomEmploye(anomalie.getEmploye().getPrenom() + " " + anomalie.getEmploye().getNom());
            dto.setBadgeEmploye(anomalie.getEmploye().getBadge());
        } else {
            // Sécurité si jamais un employé n'est pas lié
            dto.setNomEmploye("Employé non défini");
            dto.setBadgeEmploye("N/A");
        }
        
        return dto;
    }
}