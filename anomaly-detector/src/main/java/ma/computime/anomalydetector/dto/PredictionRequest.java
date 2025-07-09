// Emplacement : ma/computime/anomalydetector/dto/PredictionRequest.java
package ma.computime.anomalydetector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionRequest {

    @JsonProperty("jour_de_semaine")
    private int jourSemaine;

    @JsonProperty("jour_du_mois")
    private int jourDuMois;
    
    @JsonProperty("mois")
    private int mois;
    
    @JsonProperty("semaine_de_annee")
    private int semaineDeAnnee;
    
    @JsonProperty("badge") // <-- NOUVEAU CHAMP AJOUTÉ
    private String badge;
}