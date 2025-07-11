package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.dto.AnomalieDto;
import ma.computime.anomalydetector.dto.AnomalieMapper;
import ma.computime.anomalydetector.entity.StatutAnomalie;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.Collectors;

/**
 * Ce controller gère l'affichage des pages web (HTML) et non des données JSON.
 * Il utilise l'annotation @Controller.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AnomalieRepository anomalieRepository;

    /**
     * Prépare et affiche le tableau de bord pour un noeud hiérarchique spécifique.
     * C'est la page principale que le manager verra.
     * @param noeudId L'ID du noeud (service/équipe) à afficher.
     * @param model L'objet fourni par Spring pour passer des données à la page HTML.
     * @return Le nom du fichier HTML à afficher ("dashboard.html").
     */
    @GetMapping("/noeud/{noeudId}")
    public String getDashboardPourNoeud(@PathVariable Integer noeudId, Model model) {
        
        // On récupère les anomalies qui nécessitent une action
        var anomaliesEnAttente = anomalieRepository
                .findByNoeudConcerneIdAndStatut(noeudId, StatutAnomalie.EN_ATTENTE)
                .stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());

        // On récupère les anomalies déjà traitées pour l'historique
        var historiqueRecent = anomalieRepository
                .findByNoeudConcerneIdAndStatutNot(noeudId, StatutAnomalie.EN_ATTENTE)
                .stream()
                .map(AnomalieMapper::toAnomalieDto)
                .collect(Collectors.toList());
        
        // On "injecte" ces listes de données dans le modèle pour que la page HTML puisse les utiliser
        model.addAttribute("noeudId", noeudId);
        model.addAttribute("anomalies", anomaliesEnAttente);
        model.addAttribute("historique", historiqueRecent);
        
        // On demande à Spring/Thymeleaf de rendre le fichier "dashboard.html"
        return "dashboard"; 
    }
}