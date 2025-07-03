// Emplacement : src/main/java/ma/computime/anomalydetector/service/AnomalieDetectionService.java
package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.entity.*;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnomalieDetectionService {

    @Autowired
    private PointageRepository pointageRepository;
    @Autowired
    private EmployeRepository employeRepository;
    @Autowired
    private AnomalieRepository anomalieRepository;

    public void lancerDetectionPourTous(LocalDate jour) {
        List<Employe> tousLesEmployes = employeRepository.findAll();
        System.out.println("DEBUG: Lancement de la détection pour " + tousLesEmployes.size() + " employés le " + jour);

        for (Employe employe : tousLesEmployes) {
            List<Pointage> pointagesDuJour = pointageRepository.findByBadgeEmploye(employe.getBadge())
                    .stream()
                    .filter(pointage -> pointage.getDateMouvement().toLocalDate().equals(jour))
                    .sorted(Comparator.comparing(Pointage::getDateMouvement))
                    .collect(Collectors.toList());

            if (pointagesDuJour.isEmpty()) {
                continue;
            }

            // RÈGLE N°1 : OMISSION DE POINTAGE
            if (pointagesDuJour.size() % 2 != 0) {
                creerAnomalieSiNonExistante(employe, jour, TypeAnomalie.OMISSION_POINTAGE,
                        "Nombre de pointages impair détecté (" + pointagesDuJour.size() + " pointages).",
                        "Vérifier les pointages de la journée.");
            }
            // RÈGLES SUIVANTES (seulement si le nombre de pointages est pair)
            else {
                detecterHeuresSupplementaires(employe, jour, pointagesDuJour);
                detecterRetards(employe, jour, pointagesDuJour); // <--- Appel de la nouvelle méthode
            }
        }
    }
    
    // --- METHODES DE DETECTION SPECIFIQUES ---

    private void detecterHeuresSupplementaires(Employe employe, LocalDate jour, List<Pointage> pointagesDuJour) {
        // ... (Cette méthode reste exactement la même que celle que tu as déjà)
        Planning planning = employe.getPlanning();
        if (planning == null) return;
        String nomJourFrancais = traduireJourEnFrancais(jour.getDayOfWeek().name());
        Optional<Jour> jourTheoriqueOpt = planning.getJours().stream()
                .filter(j -> j.getLibelle().equalsIgnoreCase(nomJourFrancais))
                .findFirst();
        if (jourTheoriqueOpt.isEmpty()) return;
        Horaire horaireTheorique = jourTheoriqueOpt.get().getHoraire();
        if (horaireTheorique == null || horaireTheorique.getPlagesHoraires().isEmpty()) return;
        PlageHoraire dernierePlage = horaireTheorique.getPlagesHoraires().stream()
                .max(Comparator.comparing(PlageHoraire::getHeureFin))
                .orElse(null);
        if (dernierePlage == null) return;
        LocalTime finTheorique = dernierePlage.getHeureFin();
        Pointage dernierPointage = pointagesDuJour.get(pointagesDuJour.size() - 1);
        LocalTime dernierPointageHeure = dernierPointage.getDateMouvement().toLocalTime();
        long minutesSup = Duration.between(finTheorique, dernierPointageHeure).toMinutes();
        if (minutesSup > 15) {
            creerAnomalieSiNonExistante(employe, jour, TypeAnomalie.HEURE_SUP_NON_AUTORISEE,
                    "Détection de " + minutesSup + " minutes supplémentaires en fin de journée.",
                    "Valider " + (minutesSup / 60) + "h" + (minutesSup % 60) + "min en heures supplémentaires.");
        }
    }

    /**
     * NOUVELLE MÉTHODE POUR DÉTECTER LES RETARDS
     */
    private void detecterRetards(Employe employe, LocalDate jour, List<Pointage> pointagesDuJour) {
        Planning planning = employe.getPlanning();
        if (planning == null) return;
        String nomJourFrancais = traduireJourEnFrancais(jour.getDayOfWeek().name());
        Optional<Jour> jourTheoriqueOpt = planning.getJours().stream()
                .filter(j -> j.getLibelle().equalsIgnoreCase(nomJourFrancais))
                .findFirst();
        if (jourTheoriqueOpt.isEmpty()) return;
        Horaire horaireTheorique = jourTheoriqueOpt.get().getHoraire();
        if (horaireTheorique == null || horaireTheorique.getPlagesHoraires().isEmpty()) return;

        PlageHoraire premierePlage = horaireTheorique.getPlagesHoraires().stream()
                .min(Comparator.comparing(PlageHoraire::getHeureDebut))
                .orElse(null);
        if (premierePlage == null) return;

        LocalTime debutTheorique = premierePlage.getHeureDebut();
        Pointage premierPointage = pointagesDuJour.get(0);
        LocalTime premierPointageHeure = premierPointage.getDateMouvement().toLocalTime();
        long minutesDeRetard = Duration.between(debutTheorique, premierPointageHeure).toMinutes();
        int tolerance = premierePlage.getToleranceEntree() != null ? premierePlage.getToleranceEntree() : 0;

        if (minutesDeRetard > tolerance) {
            creerAnomalieSiNonExistante(employe, jour, TypeAnomalie.RETARD,
                    "Retard de " + minutesDeRetard + " minutes détecté (tolérance de " + tolerance + " min).",
                    "Justifier ou confirmer le retard de " + minutesDeRetard + " minutes.");
        }
    }


    // --- METHODES UTILITAIRES ---

    private void creerAnomalieSiNonExistante(Employe employe, LocalDate jour, TypeAnomalie type, String message, String suggestion) {
        boolean dejaSignale = anomalieRepository.existsByTypeAnomalieAndEmployeAndJourAnomalie(type, employe, jour);
        if (!dejaSignale) {
            Anomalie nouvelleAnomalie = new Anomalie();
            nouvelleAnomalie.setEmploye(employe);
            nouvelleAnomalie.setJourAnomalie(jour);
            nouvelleAnomalie.setTypeAnomalie(type);
            nouvelleAnomalie.setMessage(message);
            nouvelleAnomalie.setStatut(StatutAnomalie.EN_ATTENTE);
            nouvelleAnomalie.setDateCreation(LocalDateTime.now());
            nouvelleAnomalie.setSuggestion(suggestion);
            anomalieRepository.save(nouvelleAnomalie);
        }
    }
    
    private String traduireJourEnFrancais(String jourAnglais) {
        switch (jourAnglais.toUpperCase()) {
            case "MONDAY": return "Lundi";
            case "TUESDAY": return "Mardi";
            case "WEDNESDAY": return "Mercredi";
            case "THURSDAY": return "Jeudi";
            case "FRIDAY": return "Vendredi";
            case "SATURDAY": return "Samedi";
            case "SUNDAY": return "Dimanche";
            default: return "";
        }
    }
}