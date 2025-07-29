// Emplacement : src/main/java/ma/computime/anomalydetector/entity/Pointage.java

package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement")
@Data
public class Pointage {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BADGE", referencedColumnName = "BADGE", insertable = false, updatable = false)
    @JsonBackReference("employe-pointage") 
    private Employe employe;
    
    @Column(name = "BADGE")
    private String badgeEmploye;

    @Column(name = "DATE_MOUV")
    private LocalDateTime dateMouvement;
    
    @Column(name = "POINTEUSE_CODE")
    private String codePointeuse;
}