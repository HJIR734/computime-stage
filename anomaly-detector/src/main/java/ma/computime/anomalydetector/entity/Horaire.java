package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
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

    @OneToMany(mappedBy = "horaire", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    // Pas besoin de JsonManagedReference ici si on ignore les méthodes getHeure...
    private List<PlageHoraire> plagesHoraires;

    // --- MÉTHODES UTILES AJOUTÉES ---
    // Ces méthodes ne sont pas des colonnes en base, mais des "helpers".
    // L'annotation @JsonIgnore est CRUCIALE pour ne pas les inclure dans le JSON.

    @JsonIgnore
    public LocalTime getHeureDebutTheorique() {
        if (plagesHoraires == null || plagesHoraires.isEmpty()) {
            return null;
        }
        // Retourne l'heure de début de la première plage horaire
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
        // Retourne l'heure de fin de la dernière plage horaire
        return plagesHoraires.stream()
                .max(Comparator.comparing(PlageHoraire::getHeureFin))
                .map(PlageHoraire::getHeureFin)
                .orElse(null);
    }
}