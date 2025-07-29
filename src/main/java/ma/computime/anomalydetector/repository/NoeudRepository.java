// Emplacement : src/main/java/ma/computime/anomalydetector/repository/NoeudRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Noeud;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoeudRepository extends JpaRepository<Noeud, Integer> {
}