// FICHIER : Planning.java (Version Finale)

package ma.computime.anomalydetector.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

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
    private Integer categorieHeureSup;

    @Column(name = "DELTA_IN")
    private Integer deltaIn;

    @Column(name = "DELTA_PAUSE")
    private Integer deltaPause;

    @Column(name = "DELTA_OUT")
    private Integer deltaOut;

    @OneToMany(mappedBy = "planning", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference("planning-jour")
    private List<Jour> jours;

    // ===================================================================
    // DÉBUT DE LA PARTIE MODIFIÉE
    // ===================================================================

    /**
     * Trouve l'horaire correspondant à un jour de la semaine (Lundi, Mardi, etc.).
     * Cette méthode va chercher dans la liste des jours celui dont le libellé
     * correspond au jour demandé.
     *
     * @param dayOfWeek Le jour de la semaine (ex: DayOfWeek.MONDAY).
     * @return Un Optional contenant l'Horaire s'il est trouvé, sinon un Optional vide.
     */
    public Optional<Horaire> getHorairePourJour(DayOfWeek dayOfWeek) {
        if (jours == null || jours.isEmpty()) {
            return Optional.empty();
        }

        // On cherche le nom du jour en français (ex: "lundi")
        String nomJourRecherche = getFrenchDayName(dayOfWeek);

        // On parcourt la liste des jours du planning...
        return jours.stream()
                // ...et on filtre pour trouver celui qui a le bon libellé.
                .filter(jour -> jour.getLibelle() != null &&
                                normalizeString(jour.getLibelle()).equalsIgnoreCase(nomJourRecherche))
                .findFirst()
                .map(Jour::getHoraire); // Si on trouve le jour, on retourne son horaire.
    }

    /**
     * Méthode privée pour convertir une chaîne en minuscule et sans accents
     * pour une comparaison fiable.
     */
    private String normalizeString(String input) {
        // Cette ligne transforme "Mardi" en "mardi" ou "Mécredi" en "mercredi".
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    /**
     * Méthode privée pour obtenir le nom français du jour de la semaine.
     */
    private String getFrenchDayName(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "lundi";
            case TUESDAY: return "mardi";
            case WEDNESDAY: return "mercredi";
            case THURSDAY: return "jeudi";
            case FRIDAY: return "vendredi";
            case SATURDAY: return "samedi";
            case SUNDAY: return "dimanche";
            default: return "";
        }
    }
    // ===================================================================
    // FIN DE LA PARTIE MODIFIÉE
    // ===================================================================
}