// Emplacement : src/main/java/ma/computime/anomalydetector/repository/PointageRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PointageRepository extends JpaRepository<Pointage, Integer> {

    
    List<Pointage> findByBadgeEmploye(String badgeEmploye);
     
    @Query("SELECT p FROM Pointage p WHERE p.badgeEmploye = :badge AND date(p.dateMouvement) = :jour")
    List<Pointage> findForBadgeOnDay(@Param("badge") String badge, @Param("jour") LocalDate jour);
    
    
}