// Emplacement : ma/computime/anomalydetector/entity/Horaire.java
package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Duration; 
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "horaire")
@Data
public class Horaire {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "LIBELLE")
    private String libelle;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "COLOR")
    private String couleur;

    @Column(name = "TOLERANCE_ENTREE")
    private Integer toleranceEntree;

    @OneToMany(mappedBy = "horaire", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PlageHoraire> plagesHoraires;

    

    @JsonIgnore
    public LocalTime getHeureDebutTheorique() {
        if (plagesHoraires == null || plagesHoraires.isEmpty()) {
            return null;
        }
        return plagesHoraires.stream()
                .min(Comparator.comparing(PlageHoraire::getHeureDebut))
                .map(PlageHoraire::getHeureDebut)
                .orElse(null);
    }

    @JsonIgnore
    public LocalTime getHeureFinTheorique() {
        if (plagesHoraires == null || plagesHoraires.isEmpty()) {
            return null;
        }
        return plagesHoraires.stream()
                .max(Comparator.comparing(PlageHoraire::getHeureFin))
                .map(PlageHoraire::getHeureFin)
                .orElse(null);
    }
    
    
    @JsonIgnore
    public Integer getDureeTotalePauseMinutes() {
        if (plagesHoraires == null || plagesHoraires.size() < 2) {
            return 0;
        }

        
        plagesHoraires.sort(Comparator.comparing(PlageHoraire::getHeureDebut));

        long dureeTotalePauseEnMinutes = 0;
        for (int i = 0; i < plagesHoraires.size() - 1; i++) {
            LocalTime finPlageActuelle = plagesHoraires.get(i).getHeureFin();
            LocalTime debutPlageSuivante = plagesHoraires.get(i + 1).getHeureDebut();
            
            if (finPlageActuelle != null && debutPlageSuivante != null && finPlageActuelle.isBefore(debutPlageSuivante)) {
                dureeTotalePauseEnMinutes += Duration.between(finPlageActuelle, debutPlageSuivante).toMinutes();
            }
        }
        
        return (int) dureeTotalePauseEnMinutes;
    }
    
}