// Emplacement : ma/computime/anomalydetector/dto/AnomalieMapper.java
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

    // Convertit une entité Anomalie en AnomalieDto (Version MISE À JOUR)
    public static AnomalieDto toAnomalieDto(Anomalie anomalie) {
        if (anomalie == null) {
            return null;
        }
        AnomalieDto dto = new AnomalieDto();
        
        // Champs de base
        dto.setId(anomalie.getId());
        dto.setJourAnomalie(anomalie.getJourAnomalie());
        dto.setTypeAnomalie(anomalie.getTypeAnomalie());
        dto.setMessage(anomalie.getMessage());
        dto.setStatut(anomalie.getStatut());
        dto.setDateCreation(anomalie.getDateCreation());
        dto.setCommentaireValidation(anomalie.getCommentaireValidation());
        dto.setDureeEnMinutes(anomalie.getDureeEnMinutes());
        
        // Mapper l'employé associé
        dto.setEmploye(toEmployeDto(anomalie.getEmploye()));
        
        // ====================================================================
        // === MAPPING DES NOUVEAUX CHAMPS DE L'IA ===
        // ====================================================================
        dto.setDecisionIa(anomalie.getDecisionIa());
        dto.setJustificationIa(anomalie.getJustificationIa());
        dto.setValeurSuggestion(anomalie.getValeurSuggestion());
        dto.setSuggestion(anomalie.getSuggestion());
        
        return dto;
    }
}