// FICHIER : EmployeDto.java (Nouveau Fichier)
package ma.computime.anomalydetector.dto;

import lombok.Data;

// Cette classe ne contient que les infos de l'employé qu'on veut montrer.
@Data
public class EmployeDto {
    private Integer id;
    private String matricule;
    private String badge;
    private String prenom;
    private String nom;
}