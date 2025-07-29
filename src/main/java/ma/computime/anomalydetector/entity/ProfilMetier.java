// Dans entity/ProfilMetier.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "profil_metier") 
@Data
public class ProfilMetier {

    @Id
    @Column(name = "ID") 
    private Integer id;

    @Column(name = "LIBELLE") 
    private String libelle;
}