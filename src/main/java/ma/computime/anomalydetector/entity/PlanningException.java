// Emplacement : src/main/java/ma/computime/anomalydetector/entity/PlanningException.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "exception")
@Data
public class PlanningException {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "DATE_DEBUT")
    private LocalDateTime dateDebut;

    @Column(name = "DATE_FIN")
    private LocalDateTime dateFin;

    @ManyToOne
    @JoinColumn(name = "PLANNING_FK")
    private Planning planning;

    
    @ManyToMany
    @JoinTable(
        name = "user_exception", 
        joinColumns = @JoinColumn(name = "EXCEPTION_ID"), 
        inverseJoinColumns = @JoinColumn(name = "USER_ID") 
    )
    @ToString.Exclude 
    @EqualsAndHashCode.Exclude 
    private Set<Employe> employes;
}