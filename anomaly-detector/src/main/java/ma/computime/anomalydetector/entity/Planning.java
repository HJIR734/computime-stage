package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

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
    private Integer categorieHeuresSup;

    @Column(name = "DELTA_IN")
    private Integer deltaIn;

    @Column(name = "DELTA_PAUSE")
    private Integer deltaPause;

    @Column(name = "DELTA_OUT")
    private Integer deltaOut;

    @OneToMany(mappedBy = "planning", fetch = FetchType.EAGER)
    @JsonManagedReference("planning-jour") // On utilise le même nom
    private List<Jour> jours;
}