// FICHIER : AnomalieRepository.java (Code Complet et Final)
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.entity.TypeAnomalie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import ma.computime.anomalydetector.entity.StatutAnomalie;

public interface AnomalieRepository extends JpaRepository<Anomalie, Long> {

    List<Anomalie> findByStatut(StatutAnomalie statut);

    // --- NOUVELLE MÉTHODE AJOUTÉE : Filtrer par manager et statut ---
    List<Anomalie> findByNoeudConcerneIdAndStatut(Integer noeudId, StatutAnomalie statut);

    boolean existsByEmployeAndJourAnomalieAndTypeAnomalie(Employe employe, LocalDate jourAnomalie, TypeAnomalie typeAnomalie);
    List<Anomalie> findByNoeudConcerneIdAndStatutNot(Integer noeudId, StatutAnomalie statut);
     long countByEmployeAndTypeAnomalieAndStatut(Employe employe, TypeAnomalie type, StatutAnomalie statut);
    @Query("SELECT COUNT(a) FROM Anomalie a WHERE a.employe = :employe AND a.typeAnomalie = :type AND a.statut = :statut AND YEAR(a.jourAnomalie) = :year")
    long countByEmployeAndTypeAnomalieAndStatutInYear(
        @Param("employe") Employe employe, 
        @Param("type") TypeAnomalie type, 
        @Param("statut") StatutAnomalie statut, 
        @Param("year") int year
    );
}