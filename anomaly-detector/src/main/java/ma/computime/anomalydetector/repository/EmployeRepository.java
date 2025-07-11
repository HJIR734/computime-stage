package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    
    Optional<Employe> findByBadge(String badge);
    
    Optional<Employe> findByMatricule(String matricule);
}