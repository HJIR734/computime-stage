// Emplacement : src/main/java/ma/computime/anomalydetector/entity/PlageHoraire.java
package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime; // <-- NOUVEL IMPORT

@Entity
@Table(name = "plage_horaire")
@Data
public class PlageHoraire {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DEBUT")
    private Double debut;

    @Column(name = "FIN")
    private Double fin;

    @Column(name = "DEBUTPAUSE")
    private Double debutPause;

    @Column(name = "FINPAUSE")
    private Double finPause;

    @Column(name = "TOL_ENTREE")
    private Integer toleranceEntree;

    @Column(name = "TOL_SORTIE")
    private Integer toleranceSortie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMPL_HORAIRE_FK")
    @JsonBackReference // Utilise le nom par défaut car il n'y a qu'une référence vers Horaire
    private Horaire horaire;


    // ===================================================================
    // MÉTHODES UTILITAIRES POUR LA LOGIQUE MÉTIER
    // ===================================================================

    /**
     * Méthode privée pour convertir un format d'heure Double (ex: 9.5)
     * en un objet LocalTime (ex: 09:30:00) utilisable pour les calculs.
     * @param timeDouble Le temps au format Double.
     * @return Un objet LocalTime, ou null si l'entrée est null.
     */
    private LocalTime convertDoubleToLocalTime(Double timeDouble) {
        if (timeDouble == null) {
            return null;
        }
        // La partie entière représente les heures
        int hours = timeDouble.intValue();
        // La partie décimale, multipliée par 60, représente les minutes
        int minutes = (int) Math.round((timeDouble - hours) * 60);
        return LocalTime.of(hours, minutes);
    }

    /**
     * Retourne l'heure de début de la plage au format LocalTime.
     * L'annotation @JsonIgnore empêche cette méthode d'apparaître dans les réponses API JSON,
     * car elle sert uniquement à notre logique interne dans le service.
     * @return L'heure de début en LocalTime.
     */
    @JsonIgnore
    public LocalTime getHeureDebut() {
        return convertDoubleToLocalTime(this.debut);
    }

    /**
     * Retourne l'heure de fin de la plage au format LocalTime.
     * @return L'heure de fin en LocalTime.
     */
    @JsonIgnore
    public LocalTime getHeureFin() {
        return convertDoubleToLocalTime(this.fin);
    }

    /**
     * Retourne l'heure de début de la pause au format LocalTime.
     * @return L'heure de début de pause en LocalTime.
     */
    @JsonIgnore
    public LocalTime getHeureDebutPause() {
        return convertDoubleToLocalTime(this.debutPause);
    }

    /**
     * Retourne l'heure de fin de la pause au format LocalTime.
     * @return L'heure de fin de pause en LocalTime.
     */
    @JsonIgnore
    public LocalTime getHeureFinPause() {
        return convertDoubleToLocalTime(this.finPause);
    }
}