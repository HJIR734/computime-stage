// Emplacement : src/main/java/ma/computime/anomalydetector/repository/ProfilApplicatifRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.ProfilApplicatif;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilApplicatifRepository extends JpaRepository<ProfilApplicatif, Integer> {
}