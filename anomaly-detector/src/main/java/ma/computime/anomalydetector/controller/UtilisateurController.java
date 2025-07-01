package ma.computime.anomalydetector.controller;

import ma.computime.anomalydetector.entity.Utilisateur;
import ma.computime.anomalydetector.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UtilisateurController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Endpoint pour récupérer la liste de tous les utilisateurs.
     * URL : GET http://localhost:8080/api/utilisateurs
     */
    @GetMapping("/utilisateurs")
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    /**
     * Endpoint pour récupérer un utilisateur par son ID.
     * URL : GET http://localhost:8080/api/utilisateurs/469 (par exemple)
     */
    @GetMapping("/utilisateurs/{id}")
    public ResponseEntity<Utilisateur> getUtilisateurById(@PathVariable Integer id) {
        // CORRECTION ICI : on passe directement 'id' qui est déjà un Integer.
        return utilisateurRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}