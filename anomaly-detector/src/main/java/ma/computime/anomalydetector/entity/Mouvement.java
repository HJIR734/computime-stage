package ma.computime.anomalydetector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mouvement {

    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "badge", referencedColumnName = "badge", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_mouv")
    private LocalDateTime dateMouvement;

    @Column(name = "pointeuse_code")
    private String codePointeuse;

    // J'ajoute la colonne 'badge' comme un simple champ texte.
    // La relation @ManyToOne ci-dessus l'utilisera pour faire la jointure,
    // mais il est parfois utile de l'avoir directement accessible.
    @Column(name = "badge")
    private String badge;
}