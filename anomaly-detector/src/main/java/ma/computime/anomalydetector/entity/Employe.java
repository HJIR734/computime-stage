// Emplacement : src/main/java/ma/computime/anomalydetector/entity/Employe.java
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


    // --- RELATION VERS LE PLANNING DE L'EMPLOYÉ ---
    // Cette relation permettra de récupérer les horaires théoriques de travail.
    // On suppose que la colonne de jointure dans la table 'utilisateur' est 'PLANNING_FK'.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLANNING_FK")
    @JsonIgnore // On ignore cette relation dans le JSON pour éviter de surcharger les réponses.
    private Planning planning;


    // --- RELATION VERS LE MANAGER (SUPÉRIEUR HIÉRARCHIQUE) ---
    // C'est une relation sur la même table (un employé est managé par un autre employé).
    // On suppose que la colonne de jointure est 'NOEUD_FK'.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOEUD_FK")
    @JsonIgnore // Essentiel d'ignorer pour ne pas remonter toute la hiérarchie dans le JSON.
    private Employe manager;


    // --- RELATION VERS LES POINTAGES DE L'EMPLOYÉ ---
    // Un employé peut avoir plusieurs pointages.
    // C'est la classe Pointage qui "possède" la relation (via l'attribut "employe").
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    @JsonIgnore // Gère la référence pour la sérialisation JSON et évite les boucles infinies.
    private List<Pointage> pointages;

}