// FICHIER : AnomalieRepository.java (Code Complet et Final)
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.entity.TypeAnomalie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AnomalieRepository extends JpaRepository<Anomalie, Long> {

    List<Anomalie> findByStatut(StatutAnomalie statut);

    // --- NOUVELLE MÉTHODE AJOUTÉE : Filtrer par manager et statut ---
    List<Anomalie> findByManagerAssigneIdAndStatut(Integer managerId, StatutAnomalie statut);

    boolean existsByEmployeAndJourAnomalieAndTypeAnomalie(Employe employe, LocalDate jourAnomalie, TypeAnomalie typeAnomalie);
}