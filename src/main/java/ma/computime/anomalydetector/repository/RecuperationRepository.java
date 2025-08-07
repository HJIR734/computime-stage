// Emplacement : src/main/java/ma/computime/anomalydetector/repository/RecuperationRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.Recuperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface RecuperationRepository extends JpaRepository<Recuperation, Integer> {

    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Recuperation r " +
           "WHERE r.employe = :employe " +
           "AND FUNCTION('DATE', r.dateRecuperation) = :jour " +
           "AND r.statut = 'workflow_status_validated'")
    boolean existsByEmployeAndDateRecuperationAndStatutValidee(@Param("employe") Employe employe, @Param("jour") LocalDate jour);

    
}