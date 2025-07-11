// Dans ma/computime/anomalydetector/repository/EmployeRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    // Trouve un employé par son badge
    Optional<Employe> findByBadge(String badge);
    
    // Trouve un employé par son matricule
    Optional<Employe> findByMatricule(String matricule);
    @Query("SELECT e FROM Employe e LEFT JOIN FETCH e.manager")
    List<Employe> findAllWithManagers();
}