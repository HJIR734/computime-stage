// Emplacement : src/main/java/ma/computime/anomalydetector/controller/DataController.java
package ma.computime.anomalydetector.controller;

// Imports des entités
import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.Pointage;
// On a supprimé l'entité Anomalie car elle n'est plus gérée ici.

// Imports des repositories
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.PointageRepository;

// Imports techniques de Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ce contrôleur est responsable de l'exposition des données brutes de l'application,
 * comme les informations sur les employés et leurs pointages.
 * Il sert de point d'accès en lecture seule aux données de base.
 */
@RestController
@RequestMapping("/api/data") // J'ai changé le préfixe à /api/data pour éviter les conflits et être plus clair
public class DataController {

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private PointageRepository pointageRepository;

    // L'injection de AnomalieDetectionService a été supprimée.

    /**
     * Récupère la liste de tous les employés.
     * @return Une liste d'objets Employe.
     */
    @GetMapping("/employes")
    public List<Employe> getAllEmployes() {
        return employeRepository.findAll();
    }

    /**
     * Récupère les informations d'un employé spécifique en utilisant son badge.
     * @param badge Le numéro de badge de l'employé.
     * @return Un objet Employe si trouvé, sinon une erreur 404.
     */
    @GetMapping("/employes/{badge}")
    public ResponseEntity<Employe> getEmployeByBadge(@PathVariable String badge) {
        return employeRepository.findByBadge(badge)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Récupère la liste de tous les pointages (mouvements) pour un employé donné.
     * @param badge Le numéro de badge de l'employé.
     * @return Une liste d'objets Pointage.
     */
    @GetMapping("/employes/{badge}/pointages")
    public List<Pointage> getPointagesByEmployeBadge(@PathVariable String badge) {
        return pointageRepository.findByBadgeEmploye(badge);
    }
    
    // L'ancien endpoint GET /anomalies/{jour} a été complètement supprimé
    // car sa logique est maintenant dans AnomalieController.
}