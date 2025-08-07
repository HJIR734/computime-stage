// Emplacement : src/main/java/ma/computime/anomalydetector/repository/AbsenceRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Absence;
import ma.computime.anomalydetector.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ma.computime.anomalydetector.entity.Employe;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AbsenceRepository extends JpaRepository<Absence, Integer> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Absence a " +
           "WHERE a.employe = :employe " +
           "AND :jour BETWEEN FUNCTION('DATE', a.dateDebut) AND FUNCTION('DATE', a.dateReprise) " +
           "AND a.statut = 'workflow_status_validated'")
    boolean existsByEmployeAndJourCouvertAndStatutValidee(@Param("employe") Employe employe, @Param("jour") LocalDate jour);


    @Query("SELECT COUNT(a) FROM Absence a WHERE a.employe = :employe AND a.statut = 'workflow_status_validated' AND a.dateDebut >= :startDate AND a.dateDebut < :endDate")
long countRecentValidatedAbsences(@Param("employe") Employe employe, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}