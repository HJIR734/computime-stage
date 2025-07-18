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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noeud_concerne_fk")
    private Noeud noeudConcerne;

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

    // --- Colonne de suggestion originale, on peut la garder ou la commenter si non utilisée ---
    @Column(name = "suggestion_correction")
    private String suggestion;

    // ====================================================================
    // === CHAMPS DÉDIÉS AUX SUGGESTIONS DE L'IA - VERSION COMPLÈTE ===
    // ====================================================================
    
    /**
     * Stocke la décision principale de l'IA (ex: "ACCEPTER", "REJETER").
     * C'est le nouveau champ qui causait l'erreur.
     */
    @Column(name = "decision_ia", length = 50)
    private String decisionIa;

    /**
     * Stocke la justification textuelle complète fournie par l'IA.
     * C'est le deuxième nouveau champ.
     */
    @Column(name = "justification_ia", length = 1000)
    private String justificationIa;

    /**
     * Stocke la valeur concrète suggérée par l'IA (ex: heure de pointage manquant).
     * Ce champ existait déjà, il est conservé.
     */
    @Column(name = "valeur_suggestion")
    private LocalTime valeurSuggestion;

    // ====================================================================

    @Column(name = "duree_minutes")
    private Long dureeEnMinutes;
    
    @Column(name = "commentaire_validation")
    private String commentaireValidation;
    
    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_resolution")
    private LocalDateTime dateResolution;
}