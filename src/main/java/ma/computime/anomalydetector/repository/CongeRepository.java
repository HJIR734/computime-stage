// Emplacement : src/main/java/ma/computime/anomalydetector/repository/CongeRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Conge;
import ma.computime.anomalydetector.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface CongeRepository extends JpaRepository<Conge, Integer> {

    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Conge c " +
           "WHERE c.employe = :employe " +
           "AND :jour BETWEEN FUNCTION('DATE', c.dateDebut) AND FUNCTION('DATE', c.dateReprise) " +
           "AND c.statut = 'workflow_status_validated'")
    boolean existsByEmployeAndJourCouvertAndStatutValidee(@Param("employe") Employe employe, @Param("jour") LocalDate jour);
}