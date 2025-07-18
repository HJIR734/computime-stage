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

    // C'est la relation "Plusieurs-à-Plusieurs" avec les employés.
    // Elle est définie via la table de liaison "user_exception".
    @ManyToMany
    @JoinTable(
        name = "user_exception", // Le vrai nom de la table de liaison
        joinColumns = @JoinColumn(name = "EXCEPTION_ID"), // La colonne de cette entité (Exception)
        inverseJoinColumns = @JoinColumn(name = "USER_ID") // La colonne de l'autre entité (Employe)
    )
    @ToString.Exclude // Pour éviter les boucles infinies dans les logs
    @EqualsAndHashCode.Exclude // Pour éviter les boucles infinies dans les comparaisons
    private Set<Employe> employes;
}