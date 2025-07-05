// FICHIER : Jour.java
// CE FICHIER EST DÉJÀ CORRECT, AUCUN CHANGEMENT NÉCESSAIRE.

package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "jour")
@Data
public class Jour {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "LIBELLE")
    private String libelle; // Parfait, on va utiliser ce champ.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLANNING_HEBDO_FK")
    @JsonBackReference("planning-jour")
    private Planning planning;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "HORAIRE_FK")
    private Horaire horaire;
}