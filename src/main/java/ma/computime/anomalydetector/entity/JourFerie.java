// Emplacement: ma/computime/anomalydetector/entity/JourFerie.java
package ma.computime.anomalydetector.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "jr_ferie") 
@Data
public class JourFerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "ID")
    private Integer id;

    @Column(name = "LIBELLE")
    private String libelle;

    
    @Column(name = "DATE_DEBUT")
    private LocalDateTime dateDebut;
}