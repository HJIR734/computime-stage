package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Jour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JourRepository extends JpaRepository<Jour, Integer> {
}