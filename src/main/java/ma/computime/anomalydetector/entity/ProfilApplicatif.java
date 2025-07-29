// Emplacement : src/main/java/ma/computime/anomalydetector/entity/ProfilApplicatif.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "profil_authorize")
@Data
public class ProfilApplicatif {

    @Id
    @Column(name = "ID")
    private Integer id;

    
    @Column(name = "LIBELLE")
    private String libelle; 
}