package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "plage_horaire")
@Data
public class PlageHoraire {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DEBUT")
    private Double debut;

    @Column(name = "FIN")
    private Double fin;

    @Column(name = "DEBUTPAUSE")
    private Double debutPause;

    @Column(name = "FINPAUSE")
    private Double finPause;

    @Column(name = "TOL_ENTREE")
    private Integer toleranceEntree;

    @Column(name = "TOL_SORTIE")
    private Integer toleranceSortie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMPL_HORAIRE_FK")
    @JsonBackReference
    private Horaire horaire;
}