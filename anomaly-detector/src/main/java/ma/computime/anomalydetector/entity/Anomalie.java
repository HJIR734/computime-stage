// Dans ma/computime/anomalydetector/entity/Anomalie.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "anomalie")
@Data
public class Anomalie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employe_badge", referencedColumnName = "BADGE")
    private Employe employe;

    @Column(name = "jour_anomalie")
    private LocalDate jourAnomalie;

    @Enumerated(EnumType.STRING)
    private TypeAnomalie typeAnomalie;

    private String message;

    @Enumerated(EnumType.STRING)
    private StatutAnomalie statut;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "suggestion_correction")
    private String suggestion; // ex: "Ajouter pointage à 09:05" ou "Valider 2h sup"

    // Plus tard, on ajoutera l'ID du manager qui a validé, etc.
}