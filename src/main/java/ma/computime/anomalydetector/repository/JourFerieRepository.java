// Dans JourFerieRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.JourFerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface JourFerieRepository extends JpaRepository<JourFerie, Integer> {

    
    @Query("SELECT count(jf) > 0 FROM JourFerie jf WHERE date(jf.dateDebut) = :date")
    boolean existsByDate(@Param("date") LocalDate date);
}