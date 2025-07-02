// Dans ma/computime/anomalydetector/repository/PointageRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointageRepository extends JpaRepository<Pointage, Integer> { // L'ID est un Integer
    // Trouve tous les pointages pour un badge d'employé donné
    List<Pointage> findByBadgeEmploye(String badgeEmploye);
}