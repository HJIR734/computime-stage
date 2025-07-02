package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
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
    
    @OneToMany(mappedBy = "horaire", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<PlageHoraire> plagesHoraires;
}