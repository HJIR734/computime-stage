// Emplacement: ma/computime/anomalydetector/entity/JourFerie.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "jr_ferie") // Nom de la table correct
@Data
public class JourFerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Puisque la colonne est auto-incrémentée (AI)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "LIBELLE")
    private String libelle;

    // La colonne est de type DATETIME, donc on utilise LocalDateTime en Java
    @Column(name = "DATE_DEBUT")
    private LocalDateTime dateDebut;
}