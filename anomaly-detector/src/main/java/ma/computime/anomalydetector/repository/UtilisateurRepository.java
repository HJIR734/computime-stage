package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByMatricule(String matricule);

    Optional<Utilisateur> findByBadge(String badge);
}