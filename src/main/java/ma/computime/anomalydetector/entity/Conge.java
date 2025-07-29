// Emplacement : src/main/java/ma/computime/anomalydetector/entity/Conge.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "conge")
@Data
public class Conge {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_FK")
    private Employe employe;

    @Column(name = "DATE_DEBUT")
    private LocalDateTime dateDebut;

    @Column(name = "DATE_REPRISE")
    private LocalDateTime dateReprise;

    @Column(name = "STATUT")
    private String statut; 

    @Column(name = "NBR_JOUR")
    private Double nombreDeJours;
    
    
}