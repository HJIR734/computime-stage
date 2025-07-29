// Emplacement : src/main/java/ma/computime/anomalydetector/entity/Noeud.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "noeud")
@Data
public class Noeud {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "CODE")
    private String code;

    @Column(name = "LIBELLE")
    private String libelle;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOEUD_FK")
    private Noeud noeudParent;
}