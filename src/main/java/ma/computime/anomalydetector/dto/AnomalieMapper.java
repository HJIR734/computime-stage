// Emplacement : ma/computime/anomalydetector/dto/AnomalieMapper.java
package ma.computime.anomalydetector.dto;

import ma.computime.anomalydetector.entity.Anomalie;
import java.time.format.DateTimeFormatter;

public class AnomalieMapper {

    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static AnomalieDto toAnomalieDto(Anomalie anomalie) {
        if (anomalie == null) {
            return null;
        }
        
        AnomalieDto dto = new AnomalieDto();
        
    
        dto.setId(anomalie.getId());
        dto.setMessage(anomalie.getMessage());
        dto.setSuggestion(anomalie.getSuggestion()); 
        dto.setValeurSuggestion(anomalie.getValeurSuggestion()); 
        dto.setDureeEnMinutes(anomalie.getDureeEnMinutes());
        dto.setCommentaireValidation(anomalie.getCommentaireValidation());
        dto.setJourAnomalie(anomalie.getJourAnomalie().format(DATE_FORMATTER));
        dto.setTypeAnomalie(anomalie.getTypeAnomalie().toString());
        dto.setStatut(anomalie.getStatut().toString());

        
        if (anomalie.getEmploye() != null) {
            
            dto.setNomEmploye(anomalie.getEmploye().getPrenom() + " " + anomalie.getEmploye().getNom());
            dto.setBadgeEmploye(anomalie.getEmploye().getBadge());
        } else {
            
            dto.setNomEmploye("Employé non défini");
            dto.setBadgeEmploye("N/A");
        }
        
        return dto;
    }
}