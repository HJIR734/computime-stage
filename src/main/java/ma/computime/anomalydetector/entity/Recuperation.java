// Emplacement : src/main/java/ma/computime/anomalydetector/entity/Recuperation.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "recuperation")
@Data
public class Recuperation {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_FK")
    private Employe employe;

    
    @Column(name = "DATE_REC")
    private LocalDateTime dateRecuperation;

    @Column(name = "STATUT")
    private String statut; // Ex: "VALIDEE", "EN_ATTENTE", "REJETEE"

    
    @Column(name = "TM_CONTRACT")
    private Double tempsContractuel;


    @Column(name = "ABSENCE_FK")
    private Integer absenceId;

    @Column(name = "ID_PROCESS")
    private Integer processId;
}