// FICHIER : Anomalie.java (Code Complet et Final)
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "anomalie")
@Data
public class Anomalie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employe_badge", referencedColumnName = "BADGE")
    private Employe employe;

    @Column(name = "jour_anomalie")
    private LocalDate jourAnomalie;

    // --- CORRECTION CLÉ ---
    // On utilise notre Enum Java et on dit à Hibernate de le traiter comme une chaîne
    // pour qu'il corresponde au type ENUM de la base de données.
    @Enumerated(EnumType.STRING)
    @Column(name = "type_anomalie")
    private TypeAnomalie typeAnomalie;

    @Column(name = "message", length = 512)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutAnomalie statut;

    @Column(name = "suggestion_correction")
    private String suggestion;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "commentaire_validation")
    private String commentaireValidation;
}