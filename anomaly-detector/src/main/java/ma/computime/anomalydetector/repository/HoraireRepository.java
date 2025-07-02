package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Horaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoraireRepository extends JpaRepository<Horaire, Integer> {
}