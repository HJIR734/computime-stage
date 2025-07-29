// Emplacement : src/main/java/ma/computime/anomalydetector/repository/PlanningExceptionRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.PlanningException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PlanningExceptionRepository extends JpaRepository<PlanningException, Integer> {

    /**
     * Trouve une exception active pour un employé donné à une date spécifique.
     * Une exception est active si la date donnée est entre sa date de début et sa date de fin.
     */
    @Query("SELECT p FROM PlanningException p JOIN p.employes e WHERE e = :employe AND :jour BETWEEN date(p.dateDebut) AND date(p.dateFin)")
    Optional<PlanningException> findActiveExceptionForEmploye(@Param("employe") Employe employe, @Param("jour") LocalDate jour);

}