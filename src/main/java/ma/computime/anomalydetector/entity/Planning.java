// FICHIER : Planning.java (Version Finale)

package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "planning")
@Data
public class Planning {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "LIBELLE")
    private String libelle;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "CATEGHRSP")
    private Integer categorieHeureSup;

    @Column(name = "DELTA_IN")
    private Integer deltaIn;

    @Column(name = "DELTA_PAUSE")
    private Integer deltaPause;

    @Column(name = "DELTA_OUT")
    private Integer deltaOut;

    @OneToMany(mappedBy = "planning", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference("planning-jour")
    private List<Jour> jours;

    
    public Optional<Horaire> getHorairePourJour(DayOfWeek dayOfWeek) {
        if (jours == null || jours.isEmpty()) {
            return Optional.empty();
        }

        
        String nomJourRecherche = getFrenchDayName(dayOfWeek);

        
        return jours.stream()
                
                .filter(jour -> jour.getLibelle() != null &&
                                normalizeString(jour.getLibelle()).equalsIgnoreCase(nomJourRecherche))
                .findFirst()
                .map(Jour::getHoraire); 
    }

    
    private String normalizeString(String input) {
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    
    private String getFrenchDayName(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "lundi";
            case TUESDAY: return "mardi";
            case WEDNESDAY: return "mercredi";
            case THURSDAY: return "jeudi";
            case FRIDAY: return "vendredi";
            case SATURDAY: return "samedi";
            case SUNDAY: return "dimanche";
            default: return "";
        }
    }
    
}