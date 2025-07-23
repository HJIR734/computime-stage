// Emplacement : src/main/java/ma/computime/anomalydetector/repository/AbsenceRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Absence;
import ma.computime.anomalydetector.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface AbsenceRepository extends JpaRepository<Absence, Integer> {

    /**
     * Vérifie s'il existe une absence VALIDEE pour un employé qui couvre un jour donné.
     *
     * @param employe L'employé concerné.
     * @param jour Le jour à vérifier.
     * @return true si une absence validée couvre ce jour, false sinon.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Absence a " +
           "WHERE a.employe = :employe " +
           "AND :jour BETWEEN FUNCTION('DATE', a.dateDebut) AND FUNCTION('DATE', a.dateReprise) " +
           "AND a.statut = 'workflow_status_validated'")
    boolean existsByEmployeAndJourCouvertAndStatutValidee(@Param("employe") Employe employe, @Param("jour") LocalDate jour);
}