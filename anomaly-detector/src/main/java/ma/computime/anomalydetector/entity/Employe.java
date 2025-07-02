// Contenu complet du fichier Employe.java mis à jour

package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "utilisateur")
@Data
public class Employe {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MATRICULE", unique = true)
    private String matricule;

    @Column(name = "BADGE", unique = true)
    private String badge;

    @Column(name = "PRENOM")
    private String prenom;

    @Column(name = "NOM")
    private String nom;

    @Column(name = "DATE_EMB")
    private LocalDateTime dateEmbauche;

    // ----- NOUVELLE PARTIE À AJOUTER -----
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLANNING_FK")
    private Planning planning;
    // ----- FIN DE LA NOUVELLE PARTIE -----

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    @JsonManagedReference("employe-pointage") // J'ajoute un nom ici aussi
    private List<Pointage> pointages;
}