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

    /**
     * Vérifie s'il existe une récupération VALIDEE pour un employé donné à une date précise.
     * C'est la méthode qu'on utilisera pour la détection du travail un jour de repos.
     *
     * @param employe L'employé concerné.
     * @param jour La date de la récupération à vérifier.
     * @return true s'il existe une récupération validée, false sinon.
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Recuperation r " +
           "WHERE r.employe = :employe " +
           "AND FUNCTION('DATE', r.dateRecuperation) = :jour " +
           "AND r.statut = 'workflow_status_validated'")
    boolean existsByEmployeAndDateRecuperationAndStatutValidee(@Param("employe") Employe employe, @Param("jour") LocalDate jour);

    // Si on a besoin de trouver une récupération par l'ID de l'absence qu'elle compense,
    // on pourra ajouter une méthode ici plus tard.
    // Pour l'instant, on se concentre sur notre besoin immédiat.
}