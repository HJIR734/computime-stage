// Emplacement : src/main/java/ma/computime/anomalydetector/repository/AnomalieRepository.java
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

public interface AnomalieRepository extends JpaRepository<Anomalie, Long> {

    
    @Query("SELECT a FROM Anomalie a JOIN FETCH a.employe WHERE a.managerResponsable.id = :managerId AND a.statut = :statut")
    List<Anomalie> findByManagerIdAndStatutWithEmploye(Integer managerId, StatutAnomalie statut);
    
    @Query("SELECT a FROM Anomalie a JOIN FETCH a.employe WHERE a.managerResponsable.id = :managerId AND a.statut <> :statut")
    List<Anomalie> findByManagerIdAndStatutNotWithEmploye(Integer managerId, StatutAnomalie statut);

   
    @Query("SELECT a FROM Anomalie a JOIN FETCH a.employe WHERE a.noeudConcerne.id = :noeudId AND a.statut = :statut")
    List<Anomalie> findByNoeudIdAndStatutWithEmploye(Integer noeudId, StatutAnomalie statut);

    @Query("SELECT a FROM Anomalie a JOIN FETCH a.employe WHERE a.noeudConcerne.id = :noeudId AND a.statut <> :statut")
    List<Anomalie> findByNoeudIdAndStatutNotWithEmploye(Integer noeudId, StatutAnomalie statut);

    
    long countByManagerResponsableIdAndStatut(Integer managerId, StatutAnomalie statut);
    
    boolean existsByEmployeAndJourAnomalieAndTypeAnomalie(Employe employe, LocalDate jourAnomalie, TypeAnomalie typeAnomalie);

    long countByEmployeAndTypeAnomalieAndStatut(Employe employe, TypeAnomalie type, StatutAnomalie statut);

    @Query("SELECT COUNT(a) FROM Anomalie a WHERE a.employe = :employe AND a.typeAnomalie = :type AND a.statut = :statut AND YEAR(a.jourAnomalie) = :year")
    long countByEmployeAndTypeAnomalieAndStatutInYear(
        @Param("employe") Employe employe, 
        @Param("type") TypeAnomalie type, 
        @Param("statut") StatutAnomalie statut, 
        @Param("year") int year
    );
}