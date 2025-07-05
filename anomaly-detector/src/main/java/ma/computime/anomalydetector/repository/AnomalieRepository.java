// FICHIER : AnomalieRepository.java (Code Complet et Final)
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.entity.TypeAnomalie; // Importe l'Enum
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AnomalieRepository extends JpaRepository<Anomalie, Long> {

    List<Anomalie> findByStatut(StatutAnomalie statut);

    // --- CORRECTION CLÉ ---
    // La méthode accepte maintenant un objet de type TypeAnomalie,
    // ce qui correspond au Service et à l'Entité.
    boolean existsByEmployeAndJourAnomalieAndTypeAnomalie(Employe employe, LocalDate jourAnomalie, TypeAnomalie typeAnomalie);
}