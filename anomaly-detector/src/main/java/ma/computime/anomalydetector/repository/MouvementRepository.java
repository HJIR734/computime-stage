package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Mouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MouvementRepository extends JpaRepository<Mouvement, Integer> {

    List<Mouvement> findByUtilisateurId(Long utilisateurId);
    
    List<Mouvement> findByBadge(String badge);

    List<Mouvement> findByBadgeAndDateMouvementBetween(String badge, LocalDateTime dateDebut, LocalDateTime dateFin);
}