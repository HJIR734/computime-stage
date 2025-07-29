package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.Noeud;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    
    Optional<Employe> findByBadge(String badge);
    
    Optional<Employe> findByMatricule(String matricule);
    List<Employe> findByNoeud(Noeud noeud);

    

}