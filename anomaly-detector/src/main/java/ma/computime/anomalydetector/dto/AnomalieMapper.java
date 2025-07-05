// FICHIER : AnomalieMapper.java (Nouveau Fichier)
package ma.computime.anomalydetector.dto;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.Employe;

public class AnomalieMapper {

    // Convertit une entité Employe en EmployeDto
    public static EmployeDto toEmployeDto(Employe employe) {
        if (employe == null) {
            return null;
        }
        EmployeDto dto = new EmployeDto();
        dto.setId(employe.getId());
        dto.setMatricule(employe.getMatricule());
        dto.setBadge(employe.getBadge());
        dto.setPrenom(employe.getPrenom());
        dto.setNom(employe.getNom());
        return dto;
    }

    // Convertit une entité Anomalie en AnomalieDto
    public static AnomalieDto toAnomalieDto(Anomalie anomalie) {
        if (anomalie == null) {
            return null;
        }
        AnomalieDto dto = new AnomalieDto();
        dto.setId(anomalie.getId());
        dto.setJourAnomalie(anomalie.getJourAnomalie());
        dto.setTypeAnomalie(anomalie.getTypeAnomalie());
        dto.setMessage(anomalie.getMessage());
        dto.setStatut(anomalie.getStatut());
        dto.setSuggestion(anomalie.getSuggestion());
        dto.setDateCreation(anomalie.getDateCreation());
        dto.setCommentaireValidation(anomalie.getCommentaireValidation());
        
        // On utilise notre autre mapper pour l'employé
        dto.setEmploye(toEmployeDto(anomalie.getEmploye()));
        
        return dto;
    }
}