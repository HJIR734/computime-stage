// Emplacement : ma/computime/anomalydetector/entity/Anomalie.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    // --- NOUVEAU CHAMP AJOUTÉ : Le manager responsable de la validation ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noeud_concerne_fk")
    private Noeud noeudConcerne;
    // ----------------------------------------------------------------------

    @Column(name = "jour_anomalie")
    private LocalDate jourAnomalie;

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
    
    @Column(name = "valeur_suggestion")
    private LocalTime valeurSuggestion;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;
}