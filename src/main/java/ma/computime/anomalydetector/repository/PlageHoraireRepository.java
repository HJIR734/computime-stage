package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.PlageHoraire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlageHoraireRepository extends JpaRepository<PlageHoraire, Integer> {
}