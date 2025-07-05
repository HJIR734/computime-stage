package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLANNING_FK")
    @JsonIgnore // On garde JsonIgnore ici, car on ne veut pas charger le planning entier avec l'employé
    private Planning planning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOEUD_FK")
    @JsonIgnore
    private Employe manager;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    @JsonManagedReference("employe-pointage") // Nom unique pour la référence
    private List<Pointage> pointages;
}