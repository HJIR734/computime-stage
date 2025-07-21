// Dans entity/ProfilMetier.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "profil_metier") // Assure-toi que le nom de la table est correct
@Data
public class ProfilMetier {

    @Id
    @Column(name = "ID") // Assure-toi que le nom de la colonne ID est correct
    private Integer id;

    @Column(name = "LIBELLE") // Assure-toi que le nom de la colonne du libellé est correct
    private String libelle;
}