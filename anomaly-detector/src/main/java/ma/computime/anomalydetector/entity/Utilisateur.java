package ma.computime.anomalydetector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur")
@Data // Génère les getters, setters, toString, etc.
@NoArgsConstructor // Génère un constructeur sans arguments (requis par JPA)
@AllArgsConstructor // Génère un constructeur avec tous les arguments
public class Utilisateur {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "matricule")
    private String matricule;
    
    @Column(name = "badge")
    private String badge;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "date_emb")
    private LocalDateTime dateEmbauche;
}