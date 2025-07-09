// Emplacement: ma/computime/anomalydetector/repository/JourFerieRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.JourFerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface JourFerieRepository extends JpaRepository<JourFerie, Integer> {

    /**
     * Vérifie si une date donnée correspond à un jour férié.
     * La fonction DATE() en SQL permet d'ignorer la partie "heure" de la colonne DATETIME.
     * @param date La date à vérifier.
     * @return true si la date est un jour férié, false sinon.
     */
    @Query("SELECT COUNT(jf) > 0 FROM JourFerie jf WHERE FUNCTION('DATE', jf.dateDebut) = :date")
    boolean existsByDate(@Param("date") LocalDate date);
}