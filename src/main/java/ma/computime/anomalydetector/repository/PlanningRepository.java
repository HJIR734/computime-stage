// Dans ma/computime/anomalydetector/repository/PlanningRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Planning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanningRepository extends JpaRepository<Planning, Integer> {
}