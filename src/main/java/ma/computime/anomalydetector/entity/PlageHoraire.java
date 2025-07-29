// Emplacement : src/main/java/ma/computime/anomalydetector/entity/PlageHoraire.java
package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime; 

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
    @JsonBackReference 
    private Horaire horaire;


    
    private LocalTime convertDoubleToLocalTime(Double timeDouble) {
        if (timeDouble == null) {
            return null;
        }
        int hours = timeDouble.intValue();
        int minutes = (int) Math.round((timeDouble - hours) * 60);
        return LocalTime.of(hours, minutes);
    }

    
    @JsonIgnore
    public LocalTime getHeureDebut() {
        return convertDoubleToLocalTime(this.debut);
    }

    
    @JsonIgnore
    public LocalTime getHeureFin() {
        return convertDoubleToLocalTime(this.fin);
    }

    
    @JsonIgnore
    public LocalTime getHeureDebutPause() {
        return convertDoubleToLocalTime(this.debutPause);
    }

    
    @JsonIgnore
    public LocalTime getHeureFinPause() {
        return convertDoubleToLocalTime(this.finPause);
    }
}