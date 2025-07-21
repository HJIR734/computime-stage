// Emplacement : src/main/java/ma/computime/anomalydetector/entity/ProfilApplicatif.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
// Le nom de la table est probablement 'profil_applicatif' ou 'profil_authorize'.
// Il faudra vérifier dans MySQL Workbench et l'ajuster si besoin.
@Table(name = "profil_authorize")
@Data
public class ProfilApplicatif {

    @Id
    @Column(name = "ID")
    private Integer id;

    // On suppose que la table a une colonne 'libelle' ou 'nom' pour le nom du profil.
    @Column(name = "LIBELLE")
    private String libelle; // Ex: "Manager", "Collaborateur", "RH"
}